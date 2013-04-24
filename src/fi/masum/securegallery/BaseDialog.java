package fi.masum.securegallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public abstract class BaseDialog implements DialogInterface.OnClickListener
{
	public interface OnBaseDismissListener
	{
		public void onDialogDismissed(BaseDialog dialog);
	}

	protected AlertDialog.Builder m_Builder;
	protected Context m_Context;
	protected OnBaseDismissListener m_DismissListener; 
	
	public int RequestCode = 0;
	public String TitleText;
	public String BodyText;
	public String AcceptText = "Ok";
	public String CancelText = "Cancel";
	public boolean DidAccept = false;
	public boolean choiceDialog = false;
	public boolean yesNoDialog = false;
	public boolean active = false; 


	protected BaseDialog(Context context,  OnBaseDismissListener dismissListener, String title, String bodyText)
	{
		this.TitleText = title;
		this.BodyText = bodyText;
		this.m_Builder = new AlertDialog.Builder(context);
		this.m_Context = context;
		this.m_DismissListener = dismissListener;
	}

	protected void prepareDialog()
	{
		this.m_Builder.setTitle(this.TitleText);
		this.m_Builder.setPositiveButton(AcceptText, this);
		this.m_Builder.setNegativeButton(CancelText, this);
	}

	public void show()
	{
		active = true;
		this.prepareDialog();
		AlertDialog dialog = this.m_Builder.create();
		this.onCreate(dialog);
		dialog.show();
	}

	protected void onCreate(AlertDialog dialog)
	{
	}

	@Override
	public void onClick(DialogInterface dialog, int buttonId)
	{
		switch(buttonId)
		{
			case DialogInterface.BUTTON_POSITIVE:
				this.DidAccept = true;
			case DialogInterface.BUTTON_NEGATIVE:
				{
					active = false;
					dialog.dismiss();
					if (this.m_DismissListener != null)
					{
						this.m_DismissListener.onDialogDismissed(this);
					}
				} break;
			default:
				break;
		}
	}
}