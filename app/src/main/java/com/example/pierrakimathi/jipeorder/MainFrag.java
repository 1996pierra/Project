package com.example.pierrakimathi.jipeorder;


import java.io.IOException;
import java.io.InputStream;


import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;



public class MainFrag extends Fragment  {
	private Button button_add;
	private ImageView imview;
	private TextView tv,tv_descrip;

	
	private callListener mCallback;
	private Button button_view;
    

	    public interface callListener {
	        public void onButtonClick(BaseItem abc);
	    }
	    
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mCallback = (callListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnHeadlineSelectedListener");
	        }
	    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mainpagefrag, container, false);

        button_add = (Button) view.findViewById(R.id.fragment_button_center);
        button_view = (Button) view.findViewById(R.id.fragment_button_left);
        
        imview = (ImageView) view.findViewById(R.id.test_image);
        tv = (TextView) view.findViewById(R.id.title);
        tv_descrip = (TextView) view.findViewById(R.id.description);
        button_add.setOnClickListener(new OnClickListener() {
        	
        	/**
        	 * This function is getting called whenever the button on the right side is called.
        	 * a new fragment is created and  the xml resource of the currently selected item sent to it..
        	 */
            @Override
            public void onClick(View v) {

            	if(item!=null)
            		mCallback.onButtonClick(item);
                    
            }
            
        });
        
        button_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            		mCallback.onButtonClick(null);
                    
            }
            
        });
        
        return view;
    }
    public BaseItem item=null;
    public void update(XmlResourceParser xmlItem)
    {

    	
    	String s = xmlItem.getAttributeValue(null,"pic");
    	String t = xmlItem.getAttributeValue(null,"title");
    	String d = xmlItem.getAttributeValue(null,"description");
    	String p = xmlItem.getAttributeValue(null,"price");
    	item = new BaseItem(s,t,d,p);
    	loadDataFromAsset(s);
    	
    	tv.setText(t);
    	tv_descrip.setText(d);

    	imview.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fadein));
    	tv.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left));
    }
    
    public void loadDataFromAsset(String s) {

    	try {

	    	InputStream ims = getActivity().getAssets().open(s);

	    	Drawable d = Drawable.createFromStream(ims, null);

	    	imview.setImageDrawable(d);

	    	
    	}
    	catch(IOException ex) {
    		button_add.setText("ERROR?");
    		return;
    	}
    
    }
}