package org.hortonmachine.webmaps;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.StyleImpl;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hortonmachine.database.DatabaseViewer;
import org.hortonmachine.gears.io.rasterreader.OmsRasterReader;
import org.hortonmachine.gears.io.vectorreader.OmsVectorReader;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.hortonmachine.gears.utils.images.WmsWrapper;
import org.hortonmachine.gui.utils.DefaultGuiBridgeImpl;
import org.hortonmachine.gui.utils.GuiUtilities;
import org.hortonmachine.gui.utils.GuiUtilities.IOnCloseListener;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

@SuppressWarnings("unchecked")
public class WebMapsController extends WebMapsView implements IOnCloseListener {
    private WmsWrapper currentWms;

    private LinkedHashMap<String, Layer> name2LayersMap = new LinkedHashMap<>();

    private Map<String, CRSEnvelope> crsMap;

    private Map<String, StyleImpl> stylesMap;

    public WebMapsController() {
        setPreferredSize(new Dimension(1400, 800));

        init();
    }
    private void init() {
        _loadButton.addActionListener(e -> {
            String url = _getCapabilitiesField.getText();
            if (simpleCheck(url)) {
                currentWms = new WmsWrapper(url);
                name2LayersMap.clear();

                WMSCapabilities capabilities = currentWms.getCapabilities();
                String serverName = capabilities.getService().getName();
                String serverTitle = capabilities.getService().getTitle();

                _serverNameLabel.setText(serverName);
                _serverTitleLabel.setText(serverTitle);

                String first = null;
                Layer[] layers = currentWms.getLayers();
                String[] names = new String[layers.length];
                for( int i = 0; i < layers.length; i++ ) {
                    String layerName = layers[i].getName();
                    name2LayersMap.put(layerName, layers[i]);
                    if (first == null) {
                        first = layerName;
                    }
                    names[i] = layerName;
                }

                _layersCombo.setModel(new DefaultComboBoxModel<>(names));
                _layersCombo.addActionListener(ev -> {
                    String selectedLayer = _layersCombo.getSelectedItem().toString();
                    loadLayerInfo(selectedLayer);
                });
                loadLayerInfo(first);

            }
        });

        _boundsLoadButton.addActionListener(e -> {
            File[] selFile = GuiUtilities.showOpenFilesDialog(this, "Select file", false, GuiUtilities.getLastFile(),
                    new FileFilter(){

                        @Override
                        public String getDescription() {
                            return "Shapefiles";
                        }

                        @Override
                        public boolean accept( File f ) {
                            String n = f.getName();
                            for( String ext : HMConstants.SUPPORTED_VECTOR_EXTENSIONS ) {
                                if (n.toLowerCase().endsWith(ext)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
            if (selFile != null && selFile.length > 0) {
                _boundsFileField.setText(selFile[0].getAbsolutePath());
            }

        });

        _outputSaveButton.addActionListener(e -> {
            File saveFile = GuiUtilities.showSaveFileDialog(this, "Save to geotiff", GuiUtilities.getLastFile());
            if (saveFile != null) {
                _outputFileField.setText(saveFile.getAbsolutePath());
            }
        });

        _wms2tiffButton.addActionListener(e -> {

            int imageWidth = Integer.parseInt(_outputWithField.getText());
            int imageHeight = Integer.parseInt(_outputHeightField.getText());

            String filePath = _boundsFileField.getText();
            ReferencedEnvelope envelope = null;

            try {
                if (filePath.endsWith(HMConstants.SUPPORTED_VECTOR_EXTENSIONS[0])) {
                    envelope = OmsVectorReader.readEnvelope(filePath);
                } else {
                    GridCoverage2D raster = OmsRasterReader.readRaster(filePath);
                    Polygon regionPolygon = CoverageUtilities.getRegionPolygon(raster);
                    envelope = new ReferencedEnvelope(regionPolygon.getEnvelopeInternal(), raster.getCoordinateReferenceSystem());
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                GuiUtilities.showErrorMessage(this, "Could not load bounds from file: " + e2.getLocalizedMessage());
                return;
            }

            try {

                String style = "";
                Object selectedStyleObj = _stylesCombo.getSelectedItem();
                if (selectedStyleObj != null) {
                    style = selectedStyleObj.toString();
                }
                StyleImpl styleImpl = stylesMap.get(style);

                String selectedLayer = _layersCombo.getSelectedItem().toString();
                Layer layer = name2LayersMap.get(selectedLayer);

                String selectedFormat = _formatsCombo.getSelectedItem().toString();

                String epsg = _crsCombo.getSelectedItem().toString();
                CoordinateReferenceSystem crs = getCrs(epsg);

                ReferencedEnvelope env = envelope.transform(crs, true);

                GetMapRequest mapRequest = currentWms.getMapRequest(layer, selectedFormat, epsg, imageWidth, imageHeight, env,
                        null, styleImpl);
                BufferedImage image = currentWms.getImage(mapRequest);
                if (image != null) {

                    String outPath = _outputFileField.getText();
                    ImageIO.write(image, "png", new File(outPath));

                } else {
                    String message = currentWms.getMessage(mapRequest);
                    if (message.contains("ServiceException")) {
                        final Pattern pattern = Pattern.compile("<ServiceException>(.+?)</ServiceException>", Pattern.DOTALL);
                        final Matcher matcher = pattern.matcher(message);
                        matcher.find();
                        message = matcher.group(1);
                        if (message != null) {
                            message = message.trim();
                            GuiUtilities.showWarningMessage(this, message);
                            return;
                        }
                    }
                    GuiUtilities.showWarningMessage(this, "Could not retrieve image for given parameters.");
                }

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        });

        _loadPreviewButton.addActionListener(ev -> {

            int imageWidth = 256;
            int imageHeight = 256;

            try {

                String style = "";
                Object selectedStyleObj = _stylesCombo.getSelectedItem();
                if (selectedStyleObj != null) {
                    style = selectedStyleObj.toString();
                }
                StyleImpl styleImpl = stylesMap.get(style);

                String selectedLayer = _layersCombo.getSelectedItem().toString();
                Layer layer = name2LayersMap.get(selectedLayer);

                String selectedFormat = _formatsCombo.getSelectedItem().toString();

                String epsg = _crsCombo.getSelectedItem().toString();
                CoordinateReferenceSystem crs = getCrs(epsg);

                CRSEnvelope crsEnvelope = crsMap.get(epsg);
                double w = crsEnvelope.getMinX();
                double e = crsEnvelope.getMaxX();
                double s = crsEnvelope.getMinY();
                double n = crsEnvelope.getMaxY();
                ReferencedEnvelope env = new ReferencedEnvelope(w, e, s, n, crs);

                GetMapRequest mapRequest = currentWms.getMapRequest(layer, selectedFormat, epsg, imageWidth, imageHeight, env,
                        null, styleImpl);
                GuiUtilities.copyToClipboard(currentWms.getUrl(mapRequest).toString());
                BufferedImage image = currentWms.getImage(mapRequest);
                if (image != null) {
                    _previewImageLabel.setIcon(new ImageIcon(image));
                } else {
                    String message = currentWms.getMessage(mapRequest);
                    if (message.contains("ServiceException")) {
                        final Pattern pattern = Pattern.compile("<ServiceException>(.+?)</ServiceException>", Pattern.DOTALL);
                        final Matcher matcher = pattern.matcher(message);
                        matcher.find();
                        message = matcher.group(1);
                        if (message != null) {
                            message = message.trim();
                            GuiUtilities.showWarningMessage(this, message);
                            return;
                        }
                    }
                    GuiUtilities.showWarningMessage(this, "Could not retrieve image for given parameters.");
                }

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        });
    }

    private CoordinateReferenceSystem getCrs( String epsg ) throws Exception {
        if (epsg.toUpperCase().equals("EPSG:4326")) {
            return DefaultGeographicCRS.WGS84;
        }
        return CRS.decode(epsg);
    }
    private void loadLayerInfo( String layerName ) {
        Layer layer = name2LayersMap.get(layerName);

        _layerNameLabel.setText(layerName);
        _layerTitleLabel.setText(layerName);

        stylesMap = layer.getStyles().stream().collect(Collectors.toMap(s -> s.getName(), Function.identity()));
        stylesMap.put("", null);
        String[] styleNames = stylesMap.keySet().toArray(new String[0]);
        _stylesCombo.setModel(new DefaultComboBoxModel<String>(styleNames));

        List<String> formats = currentWms.getFormats();
        _formatsCombo.setModel(new DefaultComboBoxModel<>(formats.toArray(new String[0])));

        crsMap = layer.getBoundingBoxes();
        _crsCombo.setModel(new DefaultComboBoxModel<>(crsMap.keySet().toArray(new String[0])));
        _crsCombo.addActionListener(e -> {
            String crsName = _crsCombo.getSelectedItem().toString();
            loadCrsInfo(crsName);
        });
        loadCrsInfo(_crsCombo.getSelectedItem().toString());

    }

    private void loadCrsInfo( String crsName ) {
        CRSEnvelope crsEnv = crsMap.get(crsName);
        double north = crsEnv.getMaxY();
        double south = crsEnv.getMinY();
        double east = crsEnv.getMaxX();
        double west = crsEnv.getMinX();

        _northCrsLabel.setText(String.valueOf(north));
        _southCrsLabel.setText(String.valueOf(south));
        _westCrsLabel.setText(String.valueOf(west));
        _eastCrsLabel.setText(String.valueOf(east));
    }

    private boolean simpleCheck( String url ) {
        url = url.toLowerCase();
        return url.startsWith("http") && url.contains("service=wms") && url.contains("request=getcapabilities");
    }

    public JComponent asJComponent() {
        return this;
    }

    @Override
    public void onClose() {
    }

    public static void main( String[] args ) {
        GuiUtilities.setDefaultLookAndFeel();

        DefaultGuiBridgeImpl gBridge = new DefaultGuiBridgeImpl();

        final WebMapsController controller = new WebMapsController();

        final JFrame frame = gBridge.showWindow(controller.asJComponent(), "HortonMachine Web Maps Downloader");

        Class<DatabaseViewer> class1 = DatabaseViewer.class;
        ImageIcon icon = new ImageIcon(class1.getResource("/org/hortonmachine/images/hm150.png"));
        frame.setIconImage(icon.getImage());

        GuiUtilities.addClosingListener(frame, controller);

    }

}
