package org.csstudio.trends.databrowser2.propsheet;

import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.model.AxisConfig;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class AxisNameEditAction extends Action
{

    protected AxisConfig axis_config;

    public AxisNameEditAction(
            final AxisConfig axis_config
            )
    {
        super(Messages.EditAxisName, Action.AS_PUSH_BUTTON);
        this.axis_config = axis_config;
    }

    @Override
    public void run()
    {
        InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),"Re-name Axis","Axis Name",this.axis_config.getName(),null);
        if (dlg.open() == Window.OK)
            this.axis_config.setName(dlg.getValue());
    }
}
