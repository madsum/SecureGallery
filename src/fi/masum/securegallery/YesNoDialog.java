package fi.masum.securegallery;

import android.app.Activity;
import android.content.DialogInterface;

import fi.masum.securegallery.BaseDialog.OnBaseDismissListener;

public class YesNoDialog extends BaseDialog 
{
	public int ChoosedButton = 0;
	public String BodyText;
	
	public YesNoDialog(Activity activity, OnBaseDismissListener dismissListener, String title, String bodyText)
	{
		super(activity, dismissListener, title, bodyText);
		BodyText = bodyText;
		this.yesNoDialog = true;
	}
	
	@Override
	protected void prepareDialog()
	{
		super.prepareDialog();
		this.m_Builder.setMessage(BodyText);
	}	
	
	@Override
	public void onClick(DialogInterface dialog, int buttonId)
	{
		ChoosedButton = buttonId;
		super.onClick(dialog, buttonId);
	}	
}

	
