/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hortonmachine.modules;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.hortonmachine.gears.libs.exceptions.ModelsIOException;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.gears.libs.monitor.IHMProgressMonitor;
import org.hortonmachine.gears.modules.r.summary.OmsRasterSummary;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.hortonmachine.gears.utils.features.FeatureUtilities;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.hortonmachine.hmachine.modules.network.networkattributes.OmsNetworkAttributesBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;
import oms3.annotations.UI;

@Description("Module to prepare input data for the Geoframe modelling environment.")
@Author(name = "Antonello Andrea, Silvia Franceschi", contact = "http://www.hydrologis.com")
@Keywords("geoframe")
@Label(HMConstants.HYDROGEOMORPHOLOGY)
@Name("_GeoframeInputsBuilder")
@Status(40)
@License("General Public License Version 3 (GPLv3)")
public class GeoframeInputsBuilder extends HMModel {

    @Description("Input pitfiller raster map.")
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inPitfiller = null;

    @Description("Input flowdirections raster map.")
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inDrain = null;

    @Description("Input network raster map.")
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inNet = null;

    @Description("Input skyview factor raster map.")
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inSkyview = null;

    @Description("Input numbered basins raster map.")
    @UI(HMConstants.FILEIN_UI_HINT_RASTER)
    @In
    public String inBasins = null;

    @Description("Output folder for the geoframe data preparation")
    @UI(HMConstants.FOLDERIN_UI_HINT)
    @In
    public String outFolder = null;

    private boolean doOverWrite = false;

    @Execute
    public void process() throws Exception {
        checkNull(inPitfiller, inDrain, inNet, inSkyview, inBasins, outFolder);

        GridCoverage2D subBasins = getRaster(inBasins);
        CoordinateReferenceSystem crs = subBasins.getCoordinateReferenceSystem();
        pm.beginTask("Vectorize raster map...", IHMProgressMonitor.UNKNOWN);
        List<Polygon> cells = CoverageUtilities.gridcoverageToCellPolygons(subBasins, null);
        pm.done();

        GridCoverage2D pit = getRaster(inPitfiller);
        GridCoverage2D sky = getRaster(inSkyview);
        GridCoverage2D drain = getRaster(inDrain);
        GridCoverage2D net = getRaster(inNet);

        OmsNetworkAttributesBuilder netAttributesBuilder = new OmsNetworkAttributesBuilder();
        netAttributesBuilder.inDem = pit;
        netAttributesBuilder.inFlow = drain;
        netAttributesBuilder.inNet = net;
        netAttributesBuilder.doHack = true;
        netAttributesBuilder.onlyDoSimpleGeoms = true;
        netAttributesBuilder.process();
        SimpleFeatureCollection outNet = netAttributesBuilder.outNet;

        // dumpVector(outNet,
        // "/Users/hydrologis/Dropbox/hydrologis/lavori/2020_projects/15_uniTN_basins/brenta/brenta_all/aaa.shp");

        List<Geometry> netGeometries = FeatureUtilities.featureCollectionToGeometriesList(outNet, true, "hack");

        Map<Integer, List<Polygon>> collected = cells.parallelStream()
                .filter(poly -> ((Number) poly.getUserData()).doubleValue() != HMConstants.doubleNovalue)
                .collect(Collectors.groupingBy(poly -> ((Number) poly.getUserData()).intValue()));

        SimpleFeatureBuilder basinsBuilder = getBasinsBuilder(pit.getCoordinateReferenceSystem());
        SimpleFeatureBuilder netBuilder = getNetBuilder(pit.getCoordinateReferenceSystem());

        DefaultFeatureCollection allBasins = new DefaultFeatureCollection();
        DefaultFeatureCollection allNetworks = new DefaultFeatureCollection();

        StringBuilder csvText = new StringBuilder();
        csvText.append("#id;x;y;elev_m;avgelev_m;area_km2;netlength;centroid_skyview\n");

        pm.beginTask("Extract vector basins...", collected.size());
        for( Entry<Integer, List<Polygon>> entry : collected.entrySet() ) {
            int basinNum = entry.getKey();

            List<Polygon> polygons = entry.getValue();
            Geometry basin = CascadedPolygonUnion.union(polygons);

            // extract largest basin
            double maxArea = Double.NEGATIVE_INFINITY;
            Geometry maxPolygon = basin;
            int numGeometries = basin.getNumGeometries();
            if (numGeometries > 1) {
                for( int i = 0; i < numGeometries; i++ ) {
                    Geometry geometryN = basin.getGeometryN(i);
                    double area = geometryN.getArea();
                    if (area > maxArea) {
                        maxArea = area;
                        maxPolygon = geometryN;
                    }
                }
            }

            // get network pieces inside basin
            Geometry basinBuffer = maxPolygon.buffer(1);
            PreparedGeometry preparedBasin = PreparedGeometryFactory.prepare(basinBuffer);
            List<LineString> netPieces = new ArrayList<>();
            int minHack = Integer.MAX_VALUE;
            HashMap<Integer, List<LineString>> hack4Lines = new HashMap<>();
            for( Geometry netGeom : netGeometries ) {
                if (preparedBasin.intersects(netGeom)) {
                    Geometry netIntersection = maxPolygon.intersection(netGeom);
                    for( int i = 0; i < netIntersection.getNumGeometries(); i++ ) {
                        Geometry geometryN = netIntersection.getGeometryN(i);
                        if (geometryN instanceof LineString) {
                            Object userData = geometryN.getUserData();
                            int hack = Integer.parseInt(userData.toString());
                            minHack = Math.min(minHack, hack);
                            netPieces.add((LineString) geometryN);
                            
                            List<LineString> list = hack4Lines.get(hack);
                            if(list == null) {
                                list = new ArrayList<>();
                            }
                            list.add((LineString) geometryN);
                            hack4Lines.put(hack, list);
                        }
                    }
                }
            }
            MultiLineString netLines = GeometryUtilities.gf().createMultiLineString(netPieces.toArray(new LineString[0]));
            double netLength = netLines.getLength();
            
            double mainNetLength = 0;
            List<LineString> minHackLines = hack4Lines.get(minHack);
            for( LineString minHackLine : minHackLines ) {
                mainNetLength += minHackLine.getLength();
            }

            Envelope basinEnvelope = maxPolygon.getEnvelopeInternal();

            Point centroid = maxPolygon.getCentroid();
            double areaM2 = maxPolygon.getArea();
            double areaKm2 = areaM2 / 1000000.0;

            Coordinate point = centroid.getCoordinate();
            double elev = CoverageUtilities.getValue(pit, point);
            double skyview = CoverageUtilities.getValue(sky, point);

            // Extracting raster data for each basin
            ReferencedEnvelope basinRefEnvelope = new ReferencedEnvelope(basinEnvelope, crs);
            GridCoverage2D clipped = CoverageUtilities.clipCoverage(subBasins, basinRefEnvelope);
            WritableRaster clippedWR = CoverageUtilities.renderedImage2IntWritableRaster(clipped.getRenderedImage(), false);

            RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(clipped);
            int cols = regionMap.getCols();
            int rows = regionMap.getRows();
            for( int r = 0; r < rows; r++ ) {
                for( int c = 0; c < cols; c++ ) {
                    int value = clippedWR.getSample(c, r, 0);
                    if (value != basinNum) {
                        clippedWR.setSample(c, r, 0, HMConstants.intNovalue);
                    }
                }
            }

            File basinFolder = makeBasinFolder(basinNum);

            GridCoverage2D maskCoverage = CoverageUtilities.buildCoverage("basin" + basinNum, clippedWR, regionMap, crs);

            GridCoverage2D clippedPit = CoverageUtilities.clipCoverage(pit, basinRefEnvelope);
            GridCoverage2D cutPit = CoverageUtilities.coverageValuesMapper(clippedPit, maskCoverage);
            File pitFile = new File(basinFolder, "dtm_" + basinNum + ".asc");
            if (!pitFile.exists() || doOverWrite) {
                dumpRaster(cutPit, pitFile.getAbsolutePath());
            }

            double[] minMaxAvgSum = OmsRasterSummary.getMinMaxAvgSum(cutPit);
            double avgElev = minMaxAvgSum[2];

            GridCoverage2D clippedSky = CoverageUtilities.clipCoverage(sky, basinRefEnvelope);
            GridCoverage2D cutSky = CoverageUtilities.coverageValuesMapper(clippedSky, maskCoverage);
            File skyFile = new File(basinFolder, "sky_" + basinNum + ".asc");
            if (!skyFile.exists() || doOverWrite) {
                dumpRaster(cutSky, skyFile.getAbsolutePath());
            }

            GridCoverage2D clippedDrain = CoverageUtilities.clipCoverage(drain, basinRefEnvelope);
            GridCoverage2D cutDrain = CoverageUtilities.coverageValuesMapper(clippedDrain, maskCoverage);
            File drainFile = new File(basinFolder, "drain_" + basinNum + ".asc");
            if (!drainFile.exists() || doOverWrite) {
                dumpRaster(cutDrain, drainFile.getAbsolutePath());
            }

            GridCoverage2D clippedNet = CoverageUtilities.clipCoverage(net, basinRefEnvelope);
            GridCoverage2D cutNet = CoverageUtilities.coverageValuesMapper(clippedNet, maskCoverage);
            File netFile = new File(basinFolder, "net_" + basinNum + ".asc");
            if (!netFile.exists() || doOverWrite) {
                dumpRaster(cutNet, netFile.getAbsolutePath());
            }

            // finalize feature writing
            Object[] basinValues = new Object[]{maxPolygon, basinNum, point.x, point.y, elev, avgElev, areaKm2, mainNetLength,
                    skyview};
            basinsBuilder.addAll(basinValues);
            SimpleFeature basinFeature = basinsBuilder.buildFeature(null);
            allBasins.add(basinFeature);
            DefaultFeatureCollection singleBasin = new DefaultFeatureCollection();
            singleBasin.add(basinFeature);
            File basinShpFile = new File(basinFolder, "subbasins_complete_ID_" + basinNum + ".shp");
            if (!basinShpFile.exists() || doOverWrite) {
                dumpVector(singleBasin, basinShpFile.getAbsolutePath());
            }

            Object[] netValues = new Object[]{netLines, basinNum, netLength};
            netBuilder.addAll(netValues);
            SimpleFeature netFeature = netBuilder.buildFeature(null);
            allNetworks.add(netFeature);
            DefaultFeatureCollection singleNet = new DefaultFeatureCollection();
            singleNet.add(netFeature);
            File netShpFile = new File(basinFolder, "network_complete_ID_" + basinNum + ".shp");
            if (!netShpFile.exists() || doOverWrite) {
                dumpVector(singleNet, netShpFile.getAbsolutePath());
            }

            csvText.append(basinNum).append(";");
            csvText.append(point.x).append(";");
            csvText.append(point.y).append(";");
            csvText.append(elev).append(";");
            csvText.append(avgElev).append(";");
            csvText.append(areaKm2).append(";");
            csvText.append(mainNetLength).append(";");
            csvText.append(skyview).append("\n");

            pm.worked(1);
        }
        pm.done();

        File folder = new File(outFolder);
        File basinShpFile = new File(folder, "subbasins_complete.shp");
        if (!basinShpFile.exists() || doOverWrite) {
            dumpVector(allBasins, basinShpFile.getAbsolutePath());
        }
        File netShpFile = new File(folder, "network_complete.shp");
        if (!netShpFile.exists() || doOverWrite) {
            dumpVector(allNetworks, netShpFile.getAbsolutePath());
        }
        File csvFile = new File(folder, "subbasins.csv");
        if (!csvFile.exists() || doOverWrite) {
            FileUtilities.writeFile(csvText.toString(), csvFile);
        }

    }

    private File makeBasinFolder( int basinNum ) throws ModelsIOException {
        File folder = new File(outFolder);
        File basinFolder = new File(folder, String.valueOf(basinNum));
        FileUtilities.folderCheckMakeOrDie(basinFolder.getAbsolutePath());
        return basinFolder;
    }

    private SimpleFeatureBuilder getBasinsBuilder( CoordinateReferenceSystem crs ) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("basin");
        b.setCRS(crs);
        b.add("the_geom", Polygon.class);
        b.add("basinid", Integer.class);
        b.add("centrx", Double.class);
        b.add("centry", Double.class);
        b.add("elev_m", Double.class);
        b.add("avgelev_m", Double.class);
        b.add("area_km2", Double.class);
        b.add("length_m", Double.class);
        b.add("skyview", Double.class);
        SimpleFeatureType type = b.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        return builder;
    }

    private SimpleFeatureBuilder getNetBuilder( CoordinateReferenceSystem crs ) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("net");
        b.setCRS(crs);
        b.add("the_geom", MultiLineString.class);
        b.add("basinid", Integer.class);
        b.add("length_m", Double.class);
        SimpleFeatureType type = b.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        return builder;
    }

    public static void main( String[] args ) throws Exception {
        String path = "yourbasefolder";

        String pit = path + "pitfiller.asc";
        String drain = path + "brenta_drain.asc";
        String net = path + "brenta_net_10000.asc";
        String sky = path + "brenta_skyview.asc";
        String basins = path + "mytest_pts_desiredbasins_10000000_20.asc";
        String outfolder = path + "geoframe";

        GeoframeInputsBuilder g = new GeoframeInputsBuilder();
        g.inPitfiller = pit;
        g.inDrain = drain;
        g.inNet = net;
        g.inSkyview = sky;
        g.inBasins = basins;
        g.outFolder = outfolder;
        g.process();
    }

}
