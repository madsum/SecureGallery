package fi.masum.securegallery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveUploadOperationListener;

import fi.masum.securegallery.SkyDriveObject.Visitor;
import fi.masum.securegallery.SkyDrivePhoto.Image;
import fi.masum.securegallery.BaseDialog.OnBaseDismissListener;

public class SkyDriveActivity extends ListActivity implements OnBaseDismissListener {
	
    public static final String EXTRA_PATH = "path";
    private static final int DIALOG_DOWNLOAD_ID = 0;
    private static final String HOME_FOLDER = "me/skydrive";
    private LiveConnectClient mClient;
    private SkyDriveListAdapter mPhotoAdapter;
    public String mCurrentFolderId;
    private Stack<String> mPrevFolderIds;
    
	private static final int ACTION_TAKE_PHOTO = 1;
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;
	private Bitmap mTempImg;
	private String mImagePath;
	private String mCurrentPhotoPath;
	private SkyDriveActivity mParent;
	private Context context;
	private String[] mOptions = {"Save in private gallery", "Save in photo gallery"};
	private ChoiceDialog mChoiceDlg;
	private YesNoDialog mYesNoDlg;    
	private File mTempFile;
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {        
		if( requestCode == ACTION_TAKE_PHOTO && resultCode == RESULT_OK)
		{
			handleCameraPhoto(data);		
		}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skydrive);
        
		Button btnSend = (Button) findViewById(R.id.btnCamera);
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
			@Override
			public void onClick(View v) {
				
				Intent startNewActivityOpen = new Intent(SkyDriveActivity.this,
						CameraActivity.class);
				startActivity(startNewActivityOpen);
				//String _email = txtEmail.getText().toString();
			}
			});        

        mPrevFolderIds = new Stack<String>();

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SkyDriveObject skyDriveObj = (SkyDriveObject) parent.getItemAtPosition(position);

                skyDriveObj.accept(new Visitor() {
                    @Override
                    public void visit(SkyDriveAlbum album) {
                        mPrevFolderIds.push(mCurrentFolderId);
                        loadFolder(album.getId());
                    }

                    @Override
                    public void visit(SkyDrivePhoto photo) {
                        ViewPhotoDialog dialog =
                                new ViewPhotoDialog(SkyDriveActivity.this, photo);
                        dialog.setOwnerActivity(SkyDriveActivity.this);
                        dialog.show();
                    }

                    @Override
                    public void visit(SkyDriveFolder folder) {
                        mPrevFolderIds.push(mCurrentFolderId);
                        loadFolder(folder.getId());
                    }

                    @SuppressLint("NewApi") @Override
                    public void visit(SkyDriveFile file) {
                        Bundle b = new Bundle();
                        showDialog(DIALOG_DOWNLOAD_ID, b);
                    }

                });
            }
        });

        mPhotoAdapter = new SkyDriveListAdapter(this);
        setListAdapter(mPhotoAdapter);

        SkyApplication app = (SkyApplication) getApplication();
        mClient = app.getConnectClient();
        
    	context = this.getApplicationContext();
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;
		mChoiceDlg = new ChoiceDialog(this, mOptions, this, "save phtoto", "");
		mYesNoDlg = new YesNoDialog(this, this, "Uplaod phtoto", "Do you want to uplaod to Sky Drive?");
		
		Button.OnClickListener mTakePicSOnClickListener = new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
			}
		};

	Button picBtn = (Button) findViewById(R.id.btnCamera);
	setBtnListenerOrDisable( 
			picBtn, 
			mTakePicSOnClickListener,
			MediaStore.ACTION_IMAGE_CAPTURE
	);   		
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if prev folders is empty, send the back button to the TabView activity.
            if (mPrevFolderIds.isEmpty()) {
                //return false;
            	return super.onKeyDown(keyCode, event);
            }

            loadFolder(mPrevFolderIds.pop());
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
	
	

    private class SkyDriveListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<SkyDriveObject> mSkyDriveObjs;
        private View mView;

        public SkyDriveListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSkyDriveObjs = new ArrayList<SkyDriveObject>();
        }

        /**
         * @return The underlying array of the class. If changes are made to this object and you
         * want them to be seen, call {@link #notifyDataSetChanged()}.
         */
        public ArrayList<SkyDriveObject> getSkyDriveObjs() {
            return mSkyDriveObjs;
        }

        @Override
        public int getCount() {
            return mSkyDriveObjs.size();
        }

        @Override
        public SkyDriveObject getItem(int position) {
            return mSkyDriveObjs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            SkyDriveObject skyDriveObj = getItem(position);
            mView = convertView != null ? convertView : null;

            skyDriveObj.accept(new Visitor() {
                
                @Override
                public void visit(SkyDriveFile file) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.text_x_preview);
                    setName(file);
                    setDescription(file);
                }

                @Override
                public void visit(SkyDriveFolder folder) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder);
                    setName(folder);
                    setDescription(folder);
                }

                @Override
                public void visit(SkyDrivePhoto photo) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.image_x_generic);
                    setName(photo);
                    setDescription(photo);

                    // Try to find a smaller/thumbnail and use that source
                    String thumbnailSource = null;
                    String smallSource = null;
                    for (Image image : photo.getImages()) {
                        if (image.getType().equals("small")) {
                            smallSource = image.getSource();
                        } else if (image.getType().equals("thumbnail")) {
                            thumbnailSource = image.getSource();
                        }
                    }

                    String source = thumbnailSource != null ? thumbnailSource :
                                    smallSource != null ? smallSource : null;

                    // if we do not have a thumbnail or small image, just leave.
                    if (source == null) {
                        return;
                    }

                    // Since we are doing async calls and mView is constantly changing,
                    // we need to hold on to this reference.
                    final View v = mView;
                    mClient.downloadAsync(source, new LiveDownloadOperationListener() {
                        @Override
                        public void onDownloadProgress(int totalBytes,
                                                       int bytesRemaining,
                                                       LiveDownloadOperation operation) {
                        }

                        @Override
                        public void onDownloadFailed(LiveOperationException exception,
                                                     LiveDownloadOperation operation) {
                            showToast(exception.getMessage());
                        }

                        @Override
                        public void onDownloadCompleted(LiveDownloadOperation operation) {
                            Bitmap bm = BitmapFactory.decodeStream(operation.getStream());
                            ImageView imgView = (ImageView) v.findViewById(R.id.skyDriveItemIcon);
                            imgView.setImageBitmap(bm);
                        }
                    });
                }

                @Override
                public void visit(SkyDriveAlbum album) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder_image);
                    setName(album);
                    setDescription(album);
                }


                private void setName(SkyDriveObject skyDriveObj) {
                    TextView tv = (TextView) mView.findViewById(R.id.nameTextView);
                    tv.setText(skyDriveObj.getName());
                }

                private void setDescription(SkyDriveObject skyDriveObj) {
                    String description = skyDriveObj.getDescription();
                    if (description == null) {
                        description = "No description.";
                    }

                    TextView tv = (TextView) mView.findViewById(R.id.descriptionTextView);
                    tv.setText(description);
                }

                private View inflateNewSkyDriveListItem() {
                    return mInflater.inflate(R.layout.skydrive_list_item, parent, false);
                }

                private void setIcon(int iconResId) {
                    ImageView img = (ImageView) mView.findViewById(R.id.skyDriveItemIcon);
                    img.setImageResource(iconResId);
                }
            });


            return mView;
        }
    }

    private class ViewPhotoDialog extends Dialog {
        private final SkyDrivePhoto mPhoto;

        public ViewPhotoDialog(Context context, SkyDrivePhoto photo) {
            super(context);
            assert photo != null;
            mPhoto = photo;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(mPhoto.getName());
            final ImageView imgView = new ImageView(getContext());
            addContentView(imgView,
                           new LayoutParams(LayoutParams.WRAP_CONTENT,
                                            LayoutParams.WRAP_CONTENT));

            mClient.downloadAsync(mPhoto.getSource(), new LiveDownloadOperationListener() {
                @Override
                public void onDownloadProgress(int totalBytes,
                                               int bytesRemaining,
                                               LiveDownloadOperation operation) {
                }

                @Override
                public void onDownloadFailed(LiveOperationException exception,
                                             LiveDownloadOperation operation) {
                    showToast(exception.getMessage());
                }

                @Override
                public void onDownloadCompleted(LiveDownloadOperation operation) {
                    Bitmap bm = BitmapFactory.decodeStream(operation.getStream());
                    imgView.setImageBitmap(bm);
                }
            });
        }
    }


    @Override
    protected Dialog onCreateDialog(final int id, final Bundle bundle) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_DOWNLOAD_ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Download")
                       .setMessage("This file will be downloaded to the sdcard.")
                       .setPositiveButton("OK", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog =
                                new ProgressDialog(SkyDriveActivity.this);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setMessage("Downloading...");
                        progressDialog.setCancelable(true);
                        progressDialog.show();

                        String fileId = bundle.getString(JsonKeys.ID);
                        String name = bundle.getString(JsonKeys.NAME);

                        File file = new File(Environment.getExternalStorageDirectory(), name);

                        final LiveDownloadOperation operation =
                                mClient.downloadAsync(fileId + "/content",
                                                      file,
                                                      new LiveDownloadOperationListener() {
                            @Override
                            public void onDownloadProgress(int totalBytes,
                                                           int bytesRemaining,
                                                           LiveDownloadOperation operation) {
                                int percentCompleted =
                                        computePrecentCompleted(totalBytes, bytesRemaining);

                                progressDialog.setProgress(percentCompleted);
                            }

                            @Override
                            public void onDownloadFailed(LiveOperationException exception,
                                                         LiveDownloadOperation operation) {
                                progressDialog.dismiss();
                                showToast(exception.getMessage());
                            }

                            @Override
                            public void onDownloadCompleted(LiveDownloadOperation operation) {
                                progressDialog.dismiss();
                                showToast("File downloaded.");
                            }
                        });

                        progressDialog.setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                operation.cancel();
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();
                break;
            }
        }

        if (dialog != null) {
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(id);
                }
            });
        }

        return dialog;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadFolder(HOME_FOLDER);
    }

    public void loadFolder(String folderId) {
        assert folderId != null;
        mCurrentFolderId = folderId;

        final ProgressDialog progressDialog =
                ProgressDialog.show(this, "", "Loading. Please wait...", true);

        mClient.getAsync(folderId + "/files", new LiveOperationListener() {
            @Override
            public void onComplete(LiveOperation operation) {
                progressDialog.dismiss();

                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    showToast(code + ": " + message);
                    return;
                }

                ArrayList<SkyDriveObject> skyDriveObjs = mPhotoAdapter.getSkyDriveObjs();
                skyDriveObjs.clear();

                JSONArray data = result.optJSONArray(JsonKeys.DATA);
                for (int i = 0; i < data.length(); i++) {
                    SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));
                    skyDriveObjs.add(skyDriveObj);
                }

                mPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(LiveOperationException exception, LiveOperation operation) {
                progressDialog.dismiss();

                showToast(exception.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int computePrecentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
    }

    private ProgressDialog showProgressDialog(String title, String message, boolean indeterminate) {
        return ProgressDialog.show(this, title, message, indeterminate);
    }
    
    public void uploadPhoto(String imagePath)
    {    	
        File file = new File(imagePath);

        final ProgressDialog uploadProgressDialog =
        		 new ProgressDialog(SkyDriveActivity.this);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setMessage("Uploading...");
        uploadProgressDialog.setCancelable(true);
        uploadProgressDialog.show();

        final LiveOperation operation =
                mClient.uploadAsync(mCurrentFolderId,
                                    file.getName(),
                                    file,
                                    new LiveUploadOperationListener() {
            @Override
            public void onUploadProgress(int totalBytes,
                                         int bytesRemaining,
                                         LiveOperation operation) {
                int percentCompleted = computePrecentCompleted(totalBytes, bytesRemaining);

                uploadProgressDialog.setProgress(percentCompleted);
            }

            @Override
            public void onUploadFailed(LiveOperationException exception,
                                       LiveOperation operation) {
                uploadProgressDialog.dismiss();
                showToast(exception.getMessage());
            }

            @Override
            public void onUploadCompleted(LiveOperation operation) {
                uploadProgressDialog.dismiss();

                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    showToast(code + ": " + message);
                    return;
                }

                loadFolder(mCurrentFolderId);
            }
        });

        uploadProgressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                operation.cancel();
            }
        });   
    }

    // Below is all camera implementation. 
    // It will be better to define a new activity for camera. For the time being let it as is 
    // If I have available time I will upadte later.
    
    private void dispatchTakePictureIntent(int actionCode) 
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	private void setBtnListenerOrDisable(Button btn, Button.OnClickListener onClickListener, String intentName) 
	{
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setClickable(false);
		}
	}	
    
    
	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE);
	}
    
    
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.*/
	
	public static boolean isIntentAvailable(Context context, String action) 
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}		
    
	private void handleCameraPhoto(Intent intent) 
	{
		Bundle extras = intent.getExtras();
		mTempImg = (Bitmap) extras.get("data");
		mImageView.setVisibility(View.VISIBLE);		
		mChoiceDlg.show();		
	}

	public void savePrivatePic() {
	    String fileName = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date())+ "_img.jpg";
	    mImagePath = "/data/data/fi.masum.securegallery/files/"+fileName;
	    FileOutputStream fos;
		try 
		{
			if(mTempImg != null)
			{
				fos = openFileOutput(fileName, Context.MODE_PRIVATE);
				mTempImg.compress(CompressFormat.JPEG, 90, fos);
				fos.close();
			}
		    showMsg("Pic save correctly!");
		} 
		catch (IOException e) 
		{
			Log.i("tag", "erro savePrivatePic: "+e.getMessage());
		}
		mImageView.setImageBitmap(mTempImg);
	}
	
	public void savePublicPic() 
	{
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-m-ss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File outputDir = context.getCacheDir(); // context being the Activity pointer
		try
		{
		File outputFile = File.createTempFile("prefix", "extension", outputDir);
		File mTempFile = File.createTempFile(imageFileName, ".jpg", outputDir);
		mCurrentPhotoPath = mTempFile.getAbsolutePath();
		}
		catch(Exception e)
		{
			Log.i("tag", "error: createImageFile "+e.getMessage());
		}
						
		galleryAddPic();
		mImageView.setImageBitmap(mTempImg);
	}	
		
	private void galleryAddPic() 
	{
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}	
	
	void showMsg(String msg)
	{
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(context, msg, duration);
    	toast.show();
	}
	
	private File createImageFile() 
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-m-ss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		Log.i("tag", "timeStamp "+timeStamp+" imageFileName "+imageFileName);
		// create temp directory to save img
		File outputDir = context.getCacheDir(); // context being the Activity pointer
		try
		{
		File outputFile = File.createTempFile("prefix", "extension", outputDir);
		File mTempFile = File.createTempFile(imageFileName, ".jpg", outputDir);
		}
		catch(Exception e)
		{
			Log.i("tag", "error: createImageFile "+e.getMessage());
		}		
		return mTempFile;
	}
   
 
    
    public void onDialogDismissed( BaseDialog dialog)
    {
    	if (dialog.choiceDialog)
    	{
	    	ChoiceDialog choiceDialog = (ChoiceDialog)dialog;
	    	int ret = choiceDialog.SelectedOption;    	
	    	if(choiceDialog.DidAccept)
	    	{
	    		if (choiceDialog.SelectedOption == 0 )
	    			savePrivatePic();
	    		else if (choiceDialog.SelectedOption == 1)
	    			savePublicPic();
	    			
	    	
	    		showMsg("seleteced option is accepted"+Integer.toString(ret));
	    		Log.i("tag", " choiceDialog ret = "+"seleteced option is accepted"+Integer.toString(ret));
	    	}
	    	else
	    	{
	    		showMsg("seleteced option is cancled "+Integer.toString(ret));
	    		Log.i("tag", " choiceDialog ret = "+"seleteced option is cancled "+Integer.toString(ret));
	    	}
	    	mYesNoDlg.show();
    	}
    	else if( dialog.yesNoDialog)
    	{
    		YesNoDialog yesDialog = (YesNoDialog)dialog;
    		
    		int tt = yesDialog.ChoosedButton;
    		
    		if( yesDialog.ChoosedButton == -1)
    		{
    			File photo = new File(mImagePath);
    			uploadPhoto(photo.getAbsolutePath());
    		}		
    		Log.i("tag", " YesNoDialog ret = "+Integer.toString(tt));
    	}    	
    }    
}
