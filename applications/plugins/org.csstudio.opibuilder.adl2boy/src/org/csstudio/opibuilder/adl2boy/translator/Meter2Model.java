package org.csstudio.opibuilder.adl2boy.translator;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.XMeterModel;
import org.csstudio.utility.adlparser.fileParser.ADLWidget;
import org.csstudio.utility.adlparser.fileParser.widgets.Meter;
import org.eclipse.swt.graphics.RGB;

public class Meter2Model extends AbstractADL2Model {
	XMeterModel meterModel = new XMeterModel();

	public Meter2Model(ADLWidget adlWidget, RGB[] colorMap, AbstractContainerModel parentModel) {
		super(adlWidget, colorMap, parentModel);
		parentModel.addChild(meterModel, true);
		Meter meterWidget = new Meter(adlWidget);
		if (meterWidget != null) {
			setADLObjectProps(meterWidget, meterModel);
			setADLMonitorProps(meterWidget, meterModel);
		}
		//TODO Add PV Limits to Meter2Model
		//TODO Add Label to Meter2Model
		//TODO Add color mode to Meter2Model
	}

	@Override
	public	AbstractWidgetModel getWidgetModel() {
		return meterModel;
	}

}
