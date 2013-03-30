package fi.masum.securegallery;

import android.app.Activity;
import android.content.DialogInterface;

public class ChoiceDialog extends BaseDialog
{
	public String[] mOptions;
	public int SelectedOption = -1;
	

	public ChoiceDialog(Activity activity, String[] options, OnDismissListener dismissListener, String title, String bodyText)
	{
		super(activity, dismissListener, title, bodyText);
		mOptions = options;
		this.choiceDialog = true;
	}
	
	@Override
	protected void prepareDialog()
	{
		super.prepareDialog();
		this.m_Builder.setSingleChoiceItems(this.mOptions, this.SelectedOption, this);
	}

	@Override
	public void onClick(DialogInterface dialog, int buttonId)
	{
		if (buttonId >= 0)
		{
			this.SelectedOption = buttonId;
		}
		else
		{
			super.onClick(dialog, buttonId);
		}
	}
}