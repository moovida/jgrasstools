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
package org.hortonmachine.gears.modules.v.rastercattofeatureattribute;

import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_AUTHORCONTACTS;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_AUTHORNAMES;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_DOCUMENTATION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_F_NEW_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_IN_RASTER_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_IN_VECTOR_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_KEYWORDS;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_LABEL;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_LICENSE;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_NAME;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_OUT_VECTOR_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_P_POS_DESCRIPTION;
import static org.hortonmachine.gears.i18n.GearsMessages.OMSRASTERCATTOFEATUREATTRIBUTE_STATUS;
import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.awt.image.RenderedImage;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.hortonmachine.gears.libs.exceptions.ModelsIllegalargumentException;
import org.hortonmachine.gears.libs.modules.HMModel;
import org.hortonmachine.gears.modules.r.scanline.OmsScanLineRasterizer;
import org.hortonmachine.gears.modules.r.summary.OmsRasterSummary;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.hortonmachine.gears.utils.features.FeatureExtender;
import org.hortonmachine.gears.utils.features.FeatureUtilities;
import org.hortonmachine.gears.utils.geometry.EGeometryType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

@Description(OMSRASTERCATTOFEATUREATTRIBUTE_DESCRIPTION)
@Documentation(OMSRASTERCATTOFEATUREATTRIBUTE_DOCUMENTATION)
@Author(name = OMSRASTERCATTOFEATUREATTRIBUTE_AUTHORNAMES, contact = OMSRASTERCATTOFEATUREATTRIBUTE_AUTHORCONTACTS)
@Keywords(OMSRASTERCATTOFEATUREATTRIBUTE_KEYWORDS)
@Label(OMSRASTERCATTOFEATUREATTRIBUTE_LABEL)
@Name(OMSRASTERCATTOFEATUREATTRIBUTE_NAME)
@Status(OMSRASTERCATTOFEATUREATTRIBUTE_STATUS)
@License(OMSRASTERCATTOFEATUREATTRIBUTE_LICENSE)
public class OmsRasterCatToFeatureAttribute extends HMModel {

    @Description(OMSRASTERCATTOFEATUREATTRIBUTE_IN_RASTER_DESCRIPTION)
    @In
    public GridCoverage2D inRaster;

    @Description(OMSRASTERCATTOFEATUREATTRIBUTE_IN_VECTOR_DESCRIPTION)
    @In
    public SimpleFeatureCollection inVector = null;

    @Description(OMSRASTERCATTOFEATUREATTRIBUTE_F_NEW_DESCRIPTION)
    @In
    public String fNew = "new";

    @Description(OMSRASTERCATTOFEATUREATTRIBUTE_P_POS_DESCRIPTION)
    @In
    public String pPos = MIDDLE;

    @Description(OMSRASTERCATTOFEATUREATTRIBUTE_OUT_VECTOR_DESCRIPTION)
    @Out
    public SimpleFeatureCollection outVector = null;

    private static final String MIDDLE = "middle";
    private static final String START = "start";
    private static final String END = "end";

    private RandomIter inIter = null;

    private GridGeometry2D gridGeometry;

    private CoordinateReferenceSystem crs;

    private RegionMap regionMap;

    @Execute
    public void process() throws Exception {
        if (inIter == null) {
            RenderedImage inputRI = inRaster.getRenderedImage();
            inIter = RandomIterFactory.create(inputRI, null);

            regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inRaster);
            // height = regionMap.get(ROWS).intValue();
            // width = regionMap.get(COLS).intValue();
            // xRes = regionMap.get(XRES);
            // yRes = regionMap.get(YRES);

            gridGeometry = inRaster.getGridGeometry();
            // GridSampleDimension[] sampleDimensions = inCoverage.getSampleDimensions();
            // double[] noDataValues = sampleDimensions[0].getNoDataValues();
            // System.out.println(noDataValues);
        }

        SimpleFeatureType featureType = inVector.getSchema();
        crs = inVector.getSchema().getCoordinateReferenceSystem();

        FeatureExtender fExt = null;

        Envelope2D inCoverageEnvelope = inRaster.getEnvelope2D();
        outVector = new DefaultFeatureCollection();
        FeatureIterator<SimpleFeature> featureIterator = inVector.features();
        int all = inVector.size();
        pm.beginTask("Extracting raster information...", all);
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            double value = -1;
            Coordinate c;
            Coordinate[] coordinates = geometry.getCoordinates();
            if (EGeometryType.isPoint(geometry)) {
                c = coordinates[0];
                if (!inCoverageEnvelope.contains(c.x, c.y)) {
                    continue;
                }
                value = getRasterValue(c);

                if (fExt == null)
                    fExt = new FeatureExtender(featureType, //
                            new String[]{fNew}, //
                            new Class< ? >[]{Double.class});

                SimpleFeature extendedFeature = fExt.extendFeature(feature, new Object[]{value});
                ((DefaultFeatureCollection) outVector).add(extendedFeature);
            } else if (EGeometryType.isLine(geometry)) {
                if (pPos.trim().equalsIgnoreCase(START)) {
                    c = coordinates[0];
                } else if (pPos.trim().equalsIgnoreCase(END)) {
                    c = coordinates[coordinates.length - 1];
                } else {// (pPos.trim().equalsIgnoreCase(MIDDLE)) {
                    c = coordinates[coordinates.length / 2];
                }
                if (!inCoverageEnvelope.contains(c.x, c.y)) {
                    continue;
                }
                value = getRasterValue(c);
                if (fExt == null)
                    fExt = new FeatureExtender(featureType, //
                            new String[]{fNew}, //
                            new Class< ? >[]{Double.class});
                SimpleFeature extendedFeature = fExt.extendFeature(feature, new Object[]{value});
                ((DefaultFeatureCollection) outVector).add(extendedFeature);
            } else if (EGeometryType.isPolygon(geometry)) {
                if (fExt == null) {
                    String max = fNew + "_max";
                    String min = fNew + "_min";
                    String sum = fNew + "_sum";
                    String avg = fNew + "_avg";
                    fExt = new FeatureExtender(featureType, //
                            new String[]{min, max, sum, avg}, //
                            new Class< ? >[]{Double.class, Double.class, Double.class, Double.class});
                }

                SimpleFeature singleFeature = FeatureUtilities.toDummyFeature(geometry, crs);
                SimpleFeatureCollection newCollection = new DefaultFeatureCollection();
                ((DefaultFeatureCollection) newCollection).add(singleFeature);
                OmsScanLineRasterizer raster = new OmsScanLineRasterizer();
                raster.inVector = newCollection;
                raster.inRaster = inRaster;
                // raster.pCols = regionMap.getCols();
                // raster.pRows = regionMap.getRows();
                // raster.pNorth = regionMap.getNorth();
                // raster.pSouth = regionMap.getSouth();
                // raster.pEast = regionMap.getEast();
                // raster.pWest = regionMap.getWest();
                raster.pValue = 1.0;
                raster.process();
                GridCoverage2D rasterizedVector = raster.outRaster;

                double[] minMaxAvgSum = OmsRasterSummary.getMinMaxAvgSum(rasterizedVector);
                // Point centroid = geometry.getCentroid();
                // if (geometry.contains(centroid)) {
                // c = centroid.getCoordinate();
                // } else {
                // c = coordinates[0];
                // }
                SimpleFeature extendedFeature = fExt.extendFeature(feature, new Object[]{minMaxAvgSum[0], minMaxAvgSum[1],
                        minMaxAvgSum[2], minMaxAvgSum[3]});
                ((DefaultFeatureCollection) outVector).add(extendedFeature);
            } else {
                throw new ModelsIllegalargumentException("The Geometry type is not supported.", this, pm);
            }

            pm.worked(1);
        }
        featureIterator.close();
        pm.done();

    }

    private double getRasterValue( Coordinate c ) throws TransformException {
        double value;
        GridCoordinates2D gridCoord = gridGeometry.worldToGrid(new DirectPosition2D(c.x, c.y));
        value = inIter.getSampleDouble(gridCoord.x, gridCoord.y, 0);

        // TODO make this better
        if (isNovalue(value) || value >= Float.MAX_VALUE || value <= -Float.MAX_VALUE) {
            value = -9999.0;
        }
        return value;
    }
}
