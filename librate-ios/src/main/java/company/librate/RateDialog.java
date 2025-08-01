package company.librate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import company.librate.view.BaseRatingBar;
import company.librate.view.CustomTextViewGradients;
import company.librate.view.RotationRatingBar;

import static android.content.Context.MODE_PRIVATE;

import androidx.fragment.app.FragmentActivity;

public class RateDialog extends Dialog {
    private boolean isBack;
    private Context context;
    private String supportEmail;
    private ImageView imageIcon;
    private RotationRatingBar rateBar;
    private SharedPreferences sharedPrefs;

    private static int upperBound = 4;
    public static final String KEY_IS_RATE = "key_is_rate";
    private boolean isRateAppTemp = false;
    private IListenerRate iListenerRate;

    public RateDialog(Context context, String supportEmail, boolean isBack, IListenerRate iListenerRate) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rate_ios);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        this.context = context;
        this.iListenerRate = iListenerRate;
        this.supportEmail = supportEmail;
        this.isBack = isBack;
        sharedPrefs = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        initDialog();
    }

    private void initDialog() {
        CustomTextViewGradients btnOk = (CustomTextViewGradients) findViewById(R.id.btn_ok);
        TextView btnNotNow = (TextView) findViewById(R.id.btn_not_now);
        ImageView btnCancel = findViewById(R.id.img_exit_rate);
        TextView txtAppName = (TextView) findViewById(R.id.txt_name_app);
        TextView txtTitle = (TextView) findViewById(R.id.txt_title);
        imageIcon = (ImageView) findViewById(R.id.img_icon_app);
        rateBar = (RotationRatingBar) findViewById(R.id.simpleRatingBar);
        //Util.setTextViewColor(btnOk);
        setTextColor(btnOk);
        // btnOk.isTextGradient(true);
        // (btnOk instanceof CustomTextViewGradients)
        //   btnOk.setTypeface(FontUntil.getTypeface("fonts/" + "poppins_medium.ttf", context));
        //  btnNotNow.setTypeface(FontUntil.getTypeface("fonts/" + "ios.otf", context));
        txtTitle.setTypeface(FontUntil.getTypeface("fonts/" + "ios.otf", context));
        txtAppName.setTypeface(FontUntil.getTypeface("fonts/" + "ios_semi_bold.otf", context));
//        txtAppName.setText(context.getResources().getString(R.string.app_namee));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRateAppTemp && rateBar.getRating() > 0) {
                    iListenerRate.stateRate();
                    SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE).edit();
                    editor.putBoolean(KEY_IS_RATE, true);
                    editor.apply();
                    if (rateBar.getRating() > upperBound) {
                        openMarket();
                        //notShowDialogWhenUserRateHigh();
                    }
                    //else {
                    //  sendEmail();
//                        notShowDialogWhenUserRateHigh();
                    //  }
                    dismiss();
                } else {
                    FontUntil.customToast(context, context.getString(R.string.please_rate_5_start));
                    //Toast.makeText(context, "please rate 5 stars", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBack) {
                    dismiss();
                    Log.d(TAG, "onClick: ");
                    ((Activity) context).finish();
                    iListenerRate.resetCurrentPager();
                } else
                    dismiss();
            }
        });
        rateBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                isRateAppTemp = true;
                btnOk.setText(getContext().getResources().getString(R.string.rate_now));
               /* if (ratingBar.getRating() > upperBound) {
                    //imageIcon.setImageResource(R.drawable.favorite);
                    btnOk.setText(getContext().getResources().getString(R.string.rate_now));
                } else {
                    // imageIcon.setImageResource(R.drawable.favorite2);
                    btnOk.setText(getContext().getResources().getString(R.string.feedback));
                }*/
            }
        });
    }

    private static final String TAG = "RateDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public boolean isRate() {
        return sharedPrefs.getBoolean(KEY_IS_RATE, false);
    }

    /**
     * update share not show rate when user rate this app > 2 *
     */
    private void notShowDialogWhenUserRateHigh() {
        if (isBack) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(KEY_IS_RATE, true);
            editor.apply();
            dismiss();
            ((Activity) context).finish();
        } else
            dismiss();
    }

    private void setTextColor(TextView textView) {
        int[] colors = new int[]{Color.parseColor("#4355FF"), Color.parseColor("#933DFE"),
                Color.parseColor("#FF35FD"), Color.parseColor("#FF8E61"), Color.parseColor("#FFE600"), Color.parseColor("#FF8E61")};
        TextPaint textPaint = textView.getPaint();
        float measure = textPaint.measureText(textView.getText().toString());
        Shader shader = new LinearGradient(0, 0, measure * 2, 0, colors, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(shader);
        textView.setTextColor(colors[0]);

    }

    private void openMarket() {
        final String appPackageName = context.getPackageName();
        try {

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));

            }catch (android.content.ActivityNotFoundException exception){
                Toast.makeText(context,"Not found information",Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void sendEmail() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Report (" + context.getPackageName() + ")");

        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(Intent.createChooser(emailIntent, "Send mail Report App !"));
    }

    public interface IListenerRate {
        void stateRate();

        void resetCurrentPager();
    }
}
