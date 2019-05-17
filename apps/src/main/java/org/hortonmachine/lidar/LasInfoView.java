package org.hortonmachine.lidar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class LasInfoView extends JPanel
{
   JTextField _inputPathField = new JTextField();
   JButton _loadButton = new JButton();
   JTable _headerTable = new JTable();
   JTable _firstPointTable = new JTable();
   JTextField _dtmInputPathField = new JTextField();
   JButton _loadDtmButton = new JButton();
   JTextField _outputFileField = new JTextField();
   JButton _outputSaveButton = new JButton();
   JButton _convertButton = new JButton();
   JTextField _samplingField = new JTextField();
   JTextField _classesField = new JTextField();
   JTextField _impulsesField = new JTextField();
   JTextField _intensityRangeField = new JTextField();
   JTextField _lowerThresField = new JTextField();
   JTextField _upperThresField = new JTextField();
   JTextField _westField = new JTextField();
   JTextField _eastField = new JTextField();
   JTextField _boundsFileField = new JTextField();
   JButton _boundsLoadButton = new JButton();
   JTextField _southField = new JTextField();
   JTextField _northField = new JTextField();
   JTextField _minZField = new JTextField();
   JTextField _maxZField = new JTextField();
   JLabel _previewImageLabel = new JLabel();
   JButton _loadPreviewButton = new JButton();
   JRadioButton _elevationRadio = new JRadioButton();
   ButtonGroup _buttongroup1 = new ButtonGroup();
   JRadioButton _intensityRadio = new JRadioButton();
   JRadioButton _classRadio = new JRadioButton();
   JRadioButton _impulseRadio = new JRadioButton();
   JRadioButton _ownColorRadio = new JRadioButton();
   JTextField _pointSizeField = new JTextField();

   /**
    * Default constructor
    */
   public LasInfoView()
   {
      initializePanel();
   }

   /**
    * Adds fill components to empty cells in the first row and first column of the grid.
    * This ensures that the grid spacing will be the same as shown in the designer.
    * @param cols an array of column indices in the first row where fill components should be added.
    * @param rows an array of row indices in the first column where fill components should be added.
    */
   void addFillComponents( Container panel, int[] cols, int[] rows )
   {
      Dimension filler = new Dimension(10,10);

      boolean filled_cell_11 = false;
      CellConstraints cc = new CellConstraints();
      if ( cols.length > 0 && rows.length > 0 )
      {
         if ( cols[0] == 1 && rows[0] == 1 )
         {
            /** add a rigid area  */
            panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
            filled_cell_11 = true;
         }
      }

      for( int index = 0; index < cols.length; index++ )
      {
         if ( cols[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
      }

      for( int index = 0; index < rows.length; index++ )
      {
         if ( rows[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
      }

   }

   /**
    * Helper method to load an image file from the CLASSPATH
    * @param imageName the package and name of the file to load relative to the CLASSPATH
    * @return an ImageIcon instance with the specified image file
    * @throws IllegalArgumentException if the image resource cannot be loaded.
    */
   public ImageIcon loadImage( String imageName )
   {
      try
      {
         ClassLoader classloader = getClass().getClassLoader();
         java.net.URL url = classloader.getResource( imageName );
         if ( url != null )
         {
            ImageIcon icon = new ImageIcon( url );
            return icon;
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

   /**
    * Method for recalculating the component orientation for 
    * right-to-left Locales.
    * @param orientation the component orientation to be applied
    */
   public void applyComponentOrientation( ComponentOrientation orientation )
   {
      // Not yet implemented...
      // I18NUtils.applyComponentOrientation(this, orientation);
      super.applyComponentOrientation(orientation);
   }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:200DLU:GROW(0.7),FILL:DEFAULT:NONE,FILL:160DLU:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel1(),cc.xy(2,2));
      jpanel1.add(createPanel6(),cc.xy(4,2));
      jpanel1.add(createPanel11(),cc.xy(6,2));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7 },new int[]{ 1,2,3 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel2(),cc.xywh(2,1,3,1));
      jpanel1.add(createPanel3(),cc.xywh(2,5,3,1));
      jpanel1.add(createPanel4(),cc.xywh(2,7,3,1));
      jpanel1.add(createPanel5(),cc.xywh(2,3,3,1));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5 },new int[]{ 1,2,3,4,5,6,7,8 });
      return jpanel1;
   }

   public JPanel createPanel2()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Las/Laz input file",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:32DLU:GROW(1.0),FILL:DEFAULT:NONE,FILL:32DLU:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _inputPathField.setName("inputPathField");
      jpanel1.add(_inputPathField,cc.xy(1,1));

      _loadButton.setActionCommand("load");
      _loadButton.setName("loadButton");
      _loadButton.setText("...");
      jpanel1.add(_loadButton,cc.xy(3,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
      return jpanel1;
   }

   public JPanel createPanel3()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Header Information",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","FILL:DEFAULT:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _headerTable.setName("headerTable");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(_headerTable);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xy(1,1));

      addFillComponents(jpanel1,new int[0],new int[0]);
      return jpanel1;
   }

   public JPanel createPanel4()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"First Point Information",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","FILL:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _firstPointTable.setName("firstPointTable");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(_firstPointTable);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xy(1,1));

      addFillComponents(jpanel1,new int[0],new int[0]);
      return jpanel1;
   }

   public JPanel createPanel5()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Optional DTM for delta",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:32DLU:GROW(1.0),FILL:DEFAULT:NONE,FILL:32DLU:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _dtmInputPathField.setName("dtmInputPathField");
      jpanel1.add(_dtmInputPathField,cc.xy(1,1));

      _loadDtmButton.setActionCommand("load");
      _loadDtmButton.setName("loadDtmButton");
      _loadDtmButton.setText("...");
      jpanel1.add(_loadDtmButton,cc.xy(3,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
      return jpanel1;
   }

   public JPanel createPanel6()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","FILL:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:NONE,CENTER:DEFAULT:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel7(),cc.xywh(2,4,1,2));
      jpanel1.add(createPanel8(),cc.xy(2,1));
      addFillComponents(jpanel1,new int[]{ 1,2,3 },new int[]{ 1,2,3,4,5 });
      return jpanel1;
   }

   public JPanel createPanel7()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Export region",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:24DLU:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Output file");
      jpanel1.add(jlabel1,cc.xy(2,2));

      _outputFileField.setName("outputFileField");
      jpanel1.add(_outputFileField,cc.xy(4,2));

      _outputSaveButton.setActionCommand("load");
      _outputSaveButton.setName("outputSaveButton");
      _outputSaveButton.setText("...");
      jpanel1.add(_outputSaveButton,cc.xy(6,2));

      _convertButton.setActionCommand("Load Preview");
      _convertButton.setName("convertButton");
      _convertButton.setText("Convert");
      jpanel1.add(_convertButton,cc.xywh(2,4,5,1));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6 },new int[]{ 1,2,3,4,5 });
      return jpanel1;
   }

   public JPanel createPanel8()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Filters",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel9(),cc.xy(1,1));
      jpanel1.add(createPanel10(),cc.xy(1,4));
      addFillComponents(jpanel1,new int[]{ 1 },new int[]{ 1,2,3,4 });
      return jpanel1;
   }

   public JPanel createPanel9()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,LEFT:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","FILL:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("sampling");
      jpanel1.add(jlabel1,cc.xy(2,1));

      _samplingField.setName("samplingField");
      jpanel1.add(_samplingField,cc.xy(4,1));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("classes");
      jpanel1.add(jlabel2,cc.xy(2,3));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("impulses");
      jpanel1.add(jlabel3,cc.xy(2,5));

      _classesField.setName("classesField");
      jpanel1.add(_classesField,cc.xy(4,3));

      _impulsesField.setName("impulsesField");
      jpanel1.add(_impulsesField,cc.xy(4,5));

      JLabel jlabel4 = new JLabel();
      jlabel4.setText("min, max intensity range");
      jpanel1.add(jlabel4,cc.xy(2,7));

      _intensityRangeField.setName("intensityRangeField");
      jpanel1.add(_intensityRangeField,cc.xy(4,7));

      JLabel jlabel5 = new JLabel();
      jlabel5.setText("post DTM lower threshold");
      jpanel1.add(jlabel5,cc.xy(2,9));

      _lowerThresField.setName("lowerThresField");
      jpanel1.add(_lowerThresField,cc.xy(4,9));

      JLabel jlabel6 = new JLabel();
      jlabel6.setText("post DTM upper threshold");
      jpanel1.add(jlabel6,cc.xy(2,11));

      _upperThresField.setName("upperThresField");
      jpanel1.add(_upperThresField,cc.xy(4,11));

      addFillComponents(jpanel1,new int[]{ 1,3 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12 });
      return jpanel1;
   }

   public JPanel createPanel10()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Bounds",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:4DLU:NONE,FILL:24DLU:NONE","CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("West");
      jpanel1.add(jlabel1,cc.xy(2,1));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("East");
      jpanel1.add(jlabel2,cc.xy(2,3));

      _westField.setName("westField");
      jpanel1.add(_westField,cc.xywh(4,1,3,1));

      _eastField.setName("eastField");
      jpanel1.add(_eastField,cc.xywh(4,3,3,1));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("Bounds from file");
      jpanel1.add(jlabel3,cc.xy(2,13));

      _boundsFileField.setName("boundsFileField");
      jpanel1.add(_boundsFileField,cc.xy(4,13));

      _boundsLoadButton.setActionCommand("load");
      _boundsLoadButton.setName("boundsLoadButton");
      _boundsLoadButton.setText("...");
      jpanel1.add(_boundsLoadButton,cc.xy(6,13));

      JLabel jlabel4 = new JLabel();
      jlabel4.setText("South");
      jpanel1.add(jlabel4,cc.xy(2,5));

      JLabel jlabel5 = new JLabel();
      jlabel5.setText("North");
      jpanel1.add(jlabel5,cc.xy(2,7));

      _southField.setName("southField");
      jpanel1.add(_southField,cc.xywh(4,5,3,1));

      _northField.setName("northField");
      jpanel1.add(_northField,cc.xywh(4,7,3,1));

      JLabel jlabel6 = new JLabel();
      jlabel6.setText("Min Z");
      jpanel1.add(jlabel6,cc.xy(2,9));

      JLabel jlabel7 = new JLabel();
      jlabel7.setText("Max Z");
      jpanel1.add(jlabel7,cc.xy(2,11));

      _minZField.setName("minZField");
      jpanel1.add(_minZField,cc.xywh(4,9,3,1));

      _maxZField.setName("maxZField");
      jpanel1.add(_maxZField,cc.xywh(4,11,3,1));

      addFillComponents(jpanel1,new int[]{ 1,3,5,6 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14 });
      return jpanel1;
   }

   public JPanel createPanel11()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel12(),cc.xy(1,1));
      _loadPreviewButton.setActionCommand("Load Preview");
      _loadPreviewButton.setName("loadPreviewButton");
      _loadPreviewButton.setText("Load Preview");
      jpanel1.add(_loadPreviewButton,cc.xy(1,4));

      jpanel1.add(createPanel13(),cc.xy(1,2));
      addFillComponents(jpanel1,new int[]{ 1 },new int[]{ 1,2,3 });
      return jpanel1;
   }

   public JPanel createPanel12()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Preview",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("CENTER:500PX:GROW(1.0)","CENTER:500PX:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _previewImageLabel.setName("previewImageLabel");
      jpanel1.add(_previewImageLabel,cc.xy(1,1));

      addFillComponents(jpanel1,new int[0],new int[0]);
      return jpanel1;
   }

   public JPanel createPanel13()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Preview properties",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(0.5),FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","TOP:DEFAULT:GROW(1.0)");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      jpanel1.add(createPanel14(),cc.xy(2,1));
      jpanel1.add(createPanel15(),cc.xy(4,1));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4 },new int[]{ 1 });
      return jpanel1;
   }

   public JPanel createPanel14()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Color by",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _elevationRadio.setActionCommand("elevation");
      _elevationRadio.setName("elevationRadio");
      _elevationRadio.setText("elevation");
      _buttongroup1.add(_elevationRadio);
      jpanel1.add(_elevationRadio,cc.xy(1,1));

      _intensityRadio.setActionCommand("intensity");
      _intensityRadio.setName("intensityRadio");
      _intensityRadio.setText("intensity");
      _buttongroup1.add(_intensityRadio);
      jpanel1.add(_intensityRadio,cc.xy(1,3));

      _classRadio.setActionCommand("intensity");
      _classRadio.setName("classRadio");
      _classRadio.setText("classification");
      _buttongroup1.add(_classRadio);
      jpanel1.add(_classRadio,cc.xy(1,5));

      _impulseRadio.setActionCommand("intensity");
      _impulseRadio.setName("impulseRadio");
      _impulseRadio.setText("impulse");
      _buttongroup1.add(_impulseRadio);
      jpanel1.add(_impulseRadio,cc.xy(1,7));

      _ownColorRadio.setActionCommand("intensity");
      _ownColorRadio.setName("ownColorRadio");
      _ownColorRadio.setText("own color");
      _buttongroup1.add(_ownColorRadio);
      jpanel1.add(_ownColorRadio,cc.xy(1,9));

      addFillComponents(jpanel1,new int[0],new int[]{ 2,4,6,8 });
      return jpanel1;
   }

   public JPanel createPanel15()
   {
      JPanel jpanel1 = new JPanel();
      TitledBorder titledborder1 = new TitledBorder(null,"Other",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,new Color(33,33,33));
      jpanel1.setBorder(titledborder1);
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","TOP:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("point size");
      jpanel1.add(jlabel1,cc.xy(1,1));

      _pointSizeField.setName("pointSizeField");
      jpanel1.add(_pointSizeField,cc.xy(3,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
      return jpanel1;
   }

   /**
    * Initializer
    */
   protected void initializePanel()
   {
      setLayout(new BorderLayout());
      add(createPanel(), BorderLayout.CENTER);
   }


}
