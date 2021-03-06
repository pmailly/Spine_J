package SpineJ;

import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

/**
 *
 * @author phm
 */
public class distanceJDialog extends javax.swing.JDialog {

    DendriteViewer3D_ dendrite = null;
    

    /**
     * Creates new form distanceJDialog
     */
    public distanceJDialog(DendriteViewer3D_ dend, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        dendrite = dend;
        jSpinnerMinDist.setModel(new SpinnerNumberModel(0.00, 0.00, dendrite.maxDist, 0.1));
        jSpinnerMinMap.setModel(new SpinnerNumberModel(dendrite.imgMapMin, dendrite.imgMapMin, dendrite.imgMapMax, 0.01));
        jSliderZposition.setValue(dendrite.Zposition);
        jSliderZposition.setMinimum(0);
        jSliderZposition.setMaximum(dendrite.imgMaxMerge.getNSlices());
    }

    public void setDendrite(DendriteViewer3D_ dendrite) {
        this.dendrite = dendrite;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelMinDist = new javax.swing.JLabel();
        jSpinnerMinDist = new javax.swing.JSpinner();
        jLabelMinMap = new javax.swing.JLabel();
        jSpinnerMinMap = new javax.swing.JSpinner();
        jLabelZposition = new javax.swing.JLabel();
        jSliderZposition = new javax.swing.JSlider();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Max local distance parameters");
        setModalityType(java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setResizable(false);

        jLabelMinDist.setText("Distance to dendrite border : ");

        jSpinnerMinDist.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMinDistStateChanged(evt);
            }
        });

        jLabelMinMap.setText("Distance Map :");

        jSpinnerMinMap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMinMapStateChanged(evt);
            }
        });

        jLabelZposition.setText("Z position :");

        jSliderZposition.setMajorTickSpacing(10);
        jSliderZposition.setMinorTickSpacing(5);
        jSliderZposition.setPaintLabels(true);
        jSliderZposition.setPaintTicks(true);
        jSliderZposition.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderZpositionStateChanged(evt);
            }
        });

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelMinMap, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelMinDist, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinnerMinMap, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                            .addComponent(jSpinnerMinDist)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelZposition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonOk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSliderZposition, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMinDist)
                    .addComponent(jSpinnerMinDist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMinMap)
                    .addComponent(jSpinnerMinMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSliderZposition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabelZposition)
                        .addGap(27, 27, 27)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOk))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinnerMinDistStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMinDistStateChanged
        dendrite.minDist = (Double) jSpinnerMinDist.getValue();
        dendrite.trimListMaxLocal = dendrite.selectDistMaxLocal();
    }//GEN-LAST:event_jSpinnerMinDistStateChanged

    private void jSpinnerMinMapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMinMapStateChanged
        dendrite.minDistMap = (Double) jSpinnerMinMap.getValue();
        dendrite.trimListMaxLocal = dendrite.selectDistMaxLocal();
    }//GEN-LAST:event_jSpinnerMinMapStateChanged

    private void jSliderZpositionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderZpositionStateChanged
        dendrite.Zposition = jSliderZposition.getValue();
        dendrite.imgMaxMerge.setZ(dendrite.Zposition);
    }//GEN-LAST:event_jSliderZpositionStateChanged

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.dispose();  
        dendrite.removeRedChannel();
        EditMaxLocal edit = new EditMaxLocal() {};
        edit.init();

    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.dispose();
        dendrite.imgMaxMerge.close();
        dendrite.imgMaxMerge.flush();
        dendrite.imgMaxProj.close();
        dendrite.imgMaxProj.flush();
        dendrite.mergeOrg.show();
        dendrite.projOrg.show();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * @param args the command line arguments
     */
    public void main() {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
        try {
            UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                     
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(distanceJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(distanceJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(distanceJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(distanceJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
//        //</editor-fold>
//
//        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                distanceJDialog dialog = new distanceJDialog(dendrite, new javax.swing.JFrame(), true);
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JLabel jLabelMinDist;
    private javax.swing.JLabel jLabelMinMap;
    private javax.swing.JLabel jLabelZposition;
    private javax.swing.JSlider jSliderZposition;
    private javax.swing.JSpinner jSpinnerMinDist;
    private javax.swing.JSpinner jSpinnerMinMap;
    // End of variables declaration//GEN-END:variables
}
