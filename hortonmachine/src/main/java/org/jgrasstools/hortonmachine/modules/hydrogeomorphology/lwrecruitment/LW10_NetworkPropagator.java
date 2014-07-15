/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
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
package org.jgrasstools.hortonmachine.modules.hydrogeomorphology.lwrecruitment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import oms3.annotations.Description;
import oms3.annotations.In;
import oms3.annotations.Out;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.io.vectorreader.OmsVectorReader;
import org.jgrasstools.gears.io.vectorwriter.OmsVectorWriter;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.features.FeatureExtender;
import org.jgrasstools.gears.utils.features.FeatureUtilities;
import org.jgrasstools.hortonmachine.modules.network.PfafstetterNumber;
import org.jgrasstools.hortonmachine.modules.network.networkattributes.NetworkChannel;
import org.opengis.feature.simple.SimpleFeature;

public class LW10_NetworkPropagator extends JGTModel implements LWFields {

    @Description("The input network points layer with the additional attributes vegetation height and timber volume.")
    @In
    public SimpleFeatureCollection inNetPoints = null;

    @Description("The output network points layer with the critical sections labelled in the attribute table.")
    @Out
    public SimpleFeatureCollection outNetPoints = null;

    /*
     * specify the names of the attributes fields
     */
    final String FIELD_LINKID = "linkid";
    public static final String FIELD_WIDTH = "w2"; // TODO add logical check on width to use
    public static final String FIELD_MEDIAN = "median";
    public static final String FIELD_ISCRITIC_LOCAL = "iscriticl";
    public static final String FIELD_ISCRITIC_GLOBAL = "iscriticg";
    public static final String FIELD_CRITIC_SOURCE = "critsource";

    private void process() throws Exception {

        /*
         * store the network points in a collection and map the Pfafstetter codes together with
         * the features ID in an  hashmap.
         */
        List<SimpleFeature> networkFeatures = FeatureUtilities.featureCollectionToList(inNetPoints);
        List<PfafstetterNumber> pfafstetterNumberList = new ArrayList<PfafstetterNumber>();
        HashMap<String, TreeMap<Integer, SimpleFeature>> pfafstetterNumber2FeaturesMap = new HashMap<String, TreeMap<Integer, SimpleFeature>>();

        for( SimpleFeature networkFeature : networkFeatures ) {
            Object pfaffObject = networkFeature.getAttribute(NetworkChannel.PFAFNAME);
            if (pfaffObject instanceof String) {
                String pfaffString = (String) pfaffObject;
                PfafstetterNumber pfaf = new PfafstetterNumber(pfaffString);
                if (!pfafstetterNumberList.contains(pfaf)) {
                    pfafstetterNumberList.add(pfaf);
                }
                TreeMap<Integer, SimpleFeature> featureTreeMap = pfafstetterNumber2FeaturesMap.get(pfaffString);
                if (featureTreeMap == null) {
                    featureTreeMap = new TreeMap<Integer, SimpleFeature>();
                    pfafstetterNumber2FeaturesMap.put(pfaffString, featureTreeMap);
                }
                Object linkidObj = networkFeature.getAttribute(FIELD_LINKID);
                if (linkidObj instanceof Integer) {
                    Integer linkId = (Integer) linkidObj;
                    featureTreeMap.put(linkId, networkFeature);
                }
            }
        }

        // sort the list of Pfafstetter to be ready to navigate the network
        Collections.sort(pfafstetterNumberList);

        /*
         * prepare the output feature collection as an extention of the input with 3 
         * additional attributes for critical sections
         */
        FeatureExtender ext = new FeatureExtender(inNetPoints.getSchema(), new String[]{FIELD_ISCRITIC_LOCAL,
                FIELD_ISCRITIC_GLOBAL, FIELD_CRITIC_SOURCE}, new Class[]{Integer.class, Integer.class, String.class});
        DefaultFeatureCollection outputFC = new DefaultFeatureCollection();
        /*
         * consider each link and navigate downstream each
         */
        double maxUpstreamHeight = -1;

        // create the variables to use in the cycle
        List<PfafstetterNumber> lastUpStreamPfafstetters = new ArrayList<PfafstetterNumber>();
        List<Double> lastUpStreamMaxHeights = new ArrayList<Double>();
        List<String> lastUpStreamCriticSource = new ArrayList<String>();
        /*
         * start the main cycle with the elaborations to identify the critical sections
         */
        pm.beginTask("Processing network...", pfafstetterNumberList.size());
        for( PfafstetterNumber pfafstetterNumber : pfafstetterNumberList ) {
            TreeMap<Integer, SimpleFeature> featuresMap = pfafstetterNumber2FeaturesMap.get(pfafstetterNumber.toString());

            String criticSource = null;
            for( int i = 0; i < lastUpStreamPfafstetters.size(); i++ ) {
                PfafstetterNumber lastUpStreamPfafstetter = lastUpStreamPfafstetters.get(i);
                if (pfafstetterNumber.isDownStreamOf(lastUpStreamPfafstetter)) {
                    /*
                     * if the other is directly upstream, check its max height and label 
                     * the critical section
                     */
                    double lastUpstreamHeight = lastUpStreamMaxHeights.get(i);
                    if (lastUpstreamHeight > maxUpstreamHeight) {
                        maxUpstreamHeight = lastUpstreamHeight;
                        criticSource = lastUpStreamCriticSource.get(i);
                    }
                }
            }

            SimpleFeature lastFeature;
            for( SimpleFeature feature : featuresMap.values() ) {
                String linkid = feature.getAttribute(FIELD_LINKID).toString();
                double width = (Double) feature.getAttribute(FIELD_WIDTH);
                double height = (Double) feature.getAttribute(FIELD_MEDIAN);
                if (height > maxUpstreamHeight) {
                    maxUpstreamHeight = height;
                    criticSource = pfafstetterNumber + "-" + linkid;
                }
                
                /*
                 * label the ctitical sections
                 */
                //critical from local parameters veg_h > width
                int isCriticLocal = 0;
                int isCriticGlobal = 0;
                if (height > width) {
                    isCriticLocal = 1;
                }
                //critical on vegetation coming from upstream
                if (maxUpstreamHeight > width) {
                    isCriticGlobal = 1;
                    maxUpstreamHeight = -1;
                }

                //update the field with the origin of critical sections
                if (criticSource == null)
                    criticSource = "";
                String tmpCriticSource = criticSource;
                if (isCriticGlobal == 0) {
                    tmpCriticSource = "";
                }
                SimpleFeature newFeature = ext.extendFeature(feature,
                        new Object[]{isCriticLocal, isCriticGlobal, tmpCriticSource});
                outputFC.add(newFeature);
            }
            //add the point to the list for the next step
            lastUpStreamPfafstetters.add(pfafstetterNumber);
            lastUpStreamMaxHeights.add(maxUpstreamHeight);
            lastUpStreamCriticSource.add(criticSource);

            pm.worked(1);
        }
        pm.done();

        outNetPoints = outputFC;
    }

    public static void main( String[] args ) throws Exception {

        String inNetPointsShp = "D:/lavori_tmp/gsoc/netpoints_width_bridgesdams_slope_veg_stand.shp";
        String outNetPointsShp = "D:/lavori_tmp/gsoc/netpoints_width_bridgesdams_slope_veg_stand_critical.shp";

        LW10_NetworkPropagator networkPropagator = new LW10_NetworkPropagator();
        networkPropagator.inNetPoints = OmsVectorReader.readVector(inNetPointsShp);

        networkPropagator.process();

        SimpleFeatureCollection outNetPointFC = networkPropagator.outNetPoints;
        OmsVectorWriter.writeVector(outNetPointsShp, outNetPointFC);

    }

}