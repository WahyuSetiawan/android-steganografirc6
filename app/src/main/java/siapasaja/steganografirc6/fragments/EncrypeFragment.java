package siapasaja.steganografirc6.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Inflater;

import siapasaja.steganografirc6.ProgressDialog.MmsIntent;
import siapasaja.steganografirc6.ProgressDialog.ProgressClass;
import siapasaja.steganografirc6.R;
import siapasaja.steganografirc6.algoritma.AlgorithmRC6;
import siapasaja.steganografirc6.algoritma.LSB2bit;
import siapasaja.steganografirc6.algoritma.ProgressHandler;

public class EncrypeFragment extends Fragment {
    private final int PICK_IMAGE = 1;
    private final int GALLERY_KITKAT_INTENT_CALLED = 2;
    private Bitmap sourceBitmap;
    private ImageView imageView;
    private final Handler handler = new Handler();
    private EditText plainText;
    private EditText password;
    private TextView pathText;
    private String chiperText;
    View view;
    private String absoluteFilePathSource;

    private ProgressDialog progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_encrype, container, false);

        this.plainText = (EditText) view.findViewById(R.id.pesanEncrype);
        this.password = (EditText) view.findViewById(R.id.passwordEncrype);
        this.pathText = (TextView) view.findViewById(R.id.path);

        imageView = (ImageView) view.findViewById(R.id.imageView);

        Button btnSelectImage = (Button) view.findViewById(R.id.selectImage);
        btnSelectImage.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 19) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, PICK_IMAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
                }


            }
        });

        Button btnEncrype = (Button) view.findViewById(R.id.encrype);

        btnEncrype.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (password.getText().length() < 5) {
                    Toast.makeText(getContext(), "Password Minimal 5 Karakter", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                /*
                Toast.makeText(getContext(), plainText.getText().toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(getContext(), password.getText().toString(), Toast.LENGTH_LONG).show();*/
                    chiperText = new AlgorithmRC6().runEncrype(plainText.getText().toString(), password.getText().toString());
                /*Toast.makeText(getActivity(), "Encrype : " + chiperText, Toast.LENGTH_SHORT).show();*/

                    progressDialog = new ProgressClass(getContext());
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Encoding");
                    progressDialog.show();

                    Thread tt = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Uri uri =  encode();
                            handler.post(mShowAlert);

                            MmsIntent mms = new MmsIntent(uri, getContext());
                            progressDialog.dismiss();
                            mms.send();
                        }
                    });
                    tt.start();
                }
            }

        });

        return view;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case (PICK_IMAGE):
                if (resultCode == -1) {
                    Uri photoUri = intent.getData();
                    if (photoUri != null) {
                        Cursor cursor = getContext().getContentResolver().query(
                                photoUri, null, null, null, null);
                        cursor.moveToFirst();

                        int idx = cursor
                                .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        absoluteFilePathSource = cursor.getString(idx);


                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inDither = false;
                        opt.inScaled = false;
                        opt.inDensity = 0;
                        opt.inJustDecodeBounds = false;
                        opt.inPurgeable = false;
                        opt.inSampleSize = 1;
                        opt.inScreenDensity = 0;
                        opt.inTargetDensity = 0;

                        sourceBitmap = BitmapFactory.decodeFile(absoluteFilePathSource,
                                opt);

                        imageView.setImageBitmap(sourceBitmap);
                    }
                }
                break;
            case (GALLERY_KITKAT_INTENT_CALLED):
                if (resultCode != Activity.RESULT_OK) return;
                if (null == intent) return;
                Uri selectedImageURI = intent.getData();
                BitmapFactory.Options opt = new BitmapFactory.Options();

                Cursor cursor = getContext().getContentResolver().query(
                        selectedImageURI, null, null, null, null);
                cursor.moveToFirst();

                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                absoluteFilePathSource = cursor.getString(idx);

                opt.inDither = false;
                opt.inScaled = false;
                opt.inDensity = 0;
                opt.inJustDecodeBounds = false;
                opt.inPurgeable = false;
                opt.inSampleSize = 1;
                opt.inScreenDensity = 0;
                opt.inTargetDensity = 0;

                try {
                    InputStream input = getContext().getContentResolver().openInputStream(selectedImageURI);
                    sourceBitmap = BitmapFactory.decodeStream(input, null, opt);
                    imageView.setImageBitmap(sourceBitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    final Runnable mShowAlert = new Runnable() {
        @Override
        public void run() {
            if (progressDialog != null) progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Simpan")
                    .setCancelable(false).setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    final Runnable mIncrementProgress = new Runnable() {
        @Override
        public void run() {
            progressDialog.incrementProgressBy(1);
        }
    };

    final Runnable mInitializeProgress = new Runnable() {
        @Override
        public void run() {
            progressDialog.setMax(100);
        }
    };

    final Runnable mSetInderminate = new Runnable() {
        @Override
        public void run() {
            progressDialog.setMessage("Simpan");
            progressDialog.setIndeterminate(true);
        }
    };

    private Uri encode() {
        Uri result = null;
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        int[] oneD = new int[width * height];
        sourceBitmap.getPixels(oneD, 0, width, 0, 0, width, height);
        int density = sourceBitmap.getDensity();
        sourceBitmap.recycle();

        byte[] byteImage = LSB2bit.encodeMessage(oneD, width, height, this.chiperText, new ProgressHandler() {
            private int mysize;
            private int actualsize;

            @Override
            public void setTotal(int tot) {
                mysize = tot / 50;
                handler.post(mInitializeProgress);
            }

            @Override
            public void increment(int inc) {
                actualsize += inc;
                if (actualsize % mysize == 0) {
                    handler.post(mIncrementProgress);
                }
            }

            @Override
            public void finished() {

            }
        });

        oneD = null;
        sourceBitmap = null;
        int[] oneDMod = LSB2bit.byteArrayToIntArray(byteImage);
        byteImage = null;

        Log.v("Encode", "" + oneDMod[0]);
        Log.v("Encode Alpha", "" + (oneDMod[0] >> 24 & 0xFF));
        Log.v("Encode Red", "" + (oneDMod[0] >> 16 & 0xFF));
        Log.v("Encode Green", "" + (oneDMod[0] >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (oneDMod[0] & 0xFF));

        System.gc();
        Log.v("Free memory", Runtime.getRuntime().freeMemory() + "");
        Log.v("Image mesure", (width * height * 32 / 8) + "");

        Bitmap destBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        destBitmap.setDensity(density);
        int partialProgr = height * width / 50;
        int masterIndex = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                destBitmap.setPixel(i, j, Color.argb(0xFF,
                        oneDMod[masterIndex] >> 16 & 0xFF,
                        oneDMod[masterIndex] >> 8 & 0xFF,
                        oneDMod[masterIndex] & 0xFF));
                masterIndex++;
                if (masterIndex % partialProgr == 0) {
                    handler.post(mIncrementProgress);
                }
            }
        }


        handler.post(mSetInderminate);
        Log.v("Encode", "" + destBitmap.getPixel(0, 0));
        Log.v("Encode Alpha", "" + (destBitmap.getPixel(0, 0) >> 24 & 0xFF));
        Log.v("Encode Red", "" + (destBitmap.getPixel(0, 0) >> 16 & 0xFF));
        Log.v("Encode Green", "" + (destBitmap.getPixel(0, 0) >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (destBitmap.getPixel(0, 0) & 0xFF));

        String sdcardState = android.os.Environment.getExternalStorageState();
        String destPath = null;

        int indexSepar = absoluteFilePathSource.lastIndexOf(File.separator);
        int indexPoint = absoluteFilePathSource.lastIndexOf(".");
        if (indexPoint <= 1)
            indexPoint = absoluteFilePathSource.length();
        String fileNameDest = absoluteFilePathSource.substring(indexSepar + 1, indexPoint);
        fileNameDest += "_mobistego";
        if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED))
            destPath = android.os.Environment.getExternalStorageDirectory()
                    + File.separator + fileNameDest + ".png";

        OutputStream fout = null;
        try {

            Log.v("Path", destPath);
            fout = new FileOutputStream(destPath);
            destBitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            //Media.insertImage(getContentResolver(),destPath, fileNameDest, "MobiStego Encoded");
            result = Uri.parse("file://" + destPath);
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
            fout.flush();
            fout.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        destBitmap.recycle();

        return result;

    }
}
