package siapasaja.steganografirc6.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import siapasaja.steganografirc6.R;
import siapasaja.steganografirc6.algoritma.AlgorithmRC6;
import siapasaja.steganografirc6.algoritma.LSB2bit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DecrypeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DecrypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DecrypeFragment extends Fragment {
    private String plainText;
    private TextView pesan;
    private TextView passwordText;
    private Handler handler = new Handler();

    private final int PICK_IMAGE = 1;
    private final int GALLERY_KITKAT_INTENT_CALLED = 2;
    private Bitmap sourceBitmap;
    private String absoluteFilePathSource;
    private ImageView imageView;

    private final Runnable runnableDismiss = new Runnable() {
        @Override
        public void run() {

        }
    };

    private ProgressDialog progressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decrype, container, false);

        Button selectImage = (Button) view.findViewById(R.id.selectImageD);
        Button decrype = (Button) view.findViewById(R.id.decrypeD);
        passwordText = (TextView) view.findViewById(R.id.passwordD);
        imageView = (ImageView) view.findViewById(R.id.imageViewD);
        pesan = (TextView) view.findViewById(R.id.pesanText);

        selectImage.setOnClickListener(new Button.OnClickListener() {

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

        decrype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decodeImage();
            }
        });

        return view;
    }

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

    private void decodeImage() {
        int[] pixels = new int[sourceBitmap.getWidth() * sourceBitmap.getHeight()];
        sourceBitmap.getPixels(pixels, 0, sourceBitmap.getWidth(), 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode Alpha", "" + (pixels[0] >> 24 & 0xFF));
        Log.v("Decode Red", "" + (pixels[0] >> 16 & 0xFF));
        Log.v("Decode Green", "" + (pixels[0] >> 8 & 0xFF));
        Log.v("Decode Blue", "" + (pixels[0] & 0xFF));
        Log.v("Decode", "" + pixels[0]);
        Log.v("Decode", "" + sourceBitmap.getPixel(0, 0));
        byte[] b = null;

        try {
            b = LSB2bit.convertArray(pixels);
        } catch (OutOfMemoryError er) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Image Too Large").setCancelable(false).setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        final String vvv = LSB2bit.decodeMessage(b, sourceBitmap.getWidth(), imageView.getHeight());
        if (vvv == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("no Message On this Image").setCancelable(false).setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }
                    );

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        } else {
            Log.v("Coded Message", vvv);
            Runnable runnableSetText = new Runnable() {
                @Override
                public void run() {
                    plainText = new AlgorithmRC6().runDecrype(vvv, passwordText.getText().toString());
                    //Toast.makeText(getContext(),plainText,Toast.LENGTH_SHORT).show();

                    pesan.setText(plainText.toString());
                }
            };
            handler.post(runnableSetText);
        }
    }
}
