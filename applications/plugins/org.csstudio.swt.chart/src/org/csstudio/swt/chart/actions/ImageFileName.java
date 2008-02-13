package org.csstudio.swt.chart.actions;

import org.csstudio.swt.chart.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/** Helper that prompts for an image file name.
 *  @author Kay Kasemir
 */
class ImageFileName
{
    /** File name ending (enforced) */
    final static String ending = ".png"; //$NON-NLS-1$

    /** Open "Save As" dialog for file name.
     *  @param shell Parent shell
     *  @return Valid file name or <code>null</code>.
     */
    public static String get(final Shell shell)
    {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        dlg.setText(Messages.SaveImage_ActionTitle);
        dlg.setFilterExtensions(new String[] { "*" + ending }); //$NON-NLS-1$
        dlg.setFilterNames(new String[] { "PNG" }); //$NON-NLS-1$
        final String filename = dlg.open();
        if (filename == null)
            return null;
        if (filename.toLowerCase().endsWith(ending))
            return filename;
        return filename + ending;
    }
}
