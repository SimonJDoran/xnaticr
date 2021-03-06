/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionExporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author simond
 */
public class ExportLogWindow extends javax.swing.JDialog {

	/**
	 * Creates new form ExportProgress
	 */
	public ExportLogWindow(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		addListeners();
	}
	
	private void addListeners()
   {
		cancelJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
				setVisible(false);
			}	  
		});
	}
	
	
	public void updateLogWindow(String els)
	{
		exportLogJTextArea.append(els);
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      jScrollPane1 = new javax.swing.JScrollPane();
      exportLogJTextArea = new javax.swing.JTextArea();
      cancelJButton = new javax.swing.JButton();
      exportLogJLabel = new javax.swing.JLabel();
      iconJLabel = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setBackground(new java.awt.Color(255, 255, 255));

      exportLogJTextArea.setEditable(false);
      exportLogJTextArea.setColumns(20);
      exportLogJTextArea.setFont(exportLogJTextArea.getFont());
      exportLogJTextArea.setLineWrap(true);
      exportLogJTextArea.setRows(5);
      jScrollPane1.setViewportView(exportLogJTextArea);

      cancelJButton.setText("Cancel");

      exportLogJLabel.setBackground(new java.awt.Color(255, 255, 255));
      exportLogJLabel.setFont(exportLogJLabel.getFont().deriveFont(exportLogJLabel.getFont().getStyle() | java.awt.Font.BOLD, exportLogJLabel.getFont().getSize()+7));
      exportLogJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      exportLogJLabel.setText("Export Log");
      exportLogJLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

      iconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_DataExporter_small.png"))); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelJButton)))
                  .addContainerGap())
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(iconJLabel)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                  .addComponent(exportLogJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(214, 214, 214))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(exportLogJLabel)
               .addComponent(iconJLabel))
            .addGap(34, 34, 34)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addComponent(cancelJButton)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

	/* The code below is introduced by the IDE, but is never used, as
	 * this component is only ever called by other classes, not from the
	 * command line.


		// Create and display the dialog
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				ExportLogWindow dialog = new ExportLogWindow(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}
	
	*/

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelJButton;
   private javax.swing.JLabel exportLogJLabel;
   private javax.swing.JTextArea exportLogJTextArea;
   private javax.swing.JLabel iconJLabel;
   private javax.swing.JScrollPane jScrollPane1;
   // End of variables declaration//GEN-END:variables
}
