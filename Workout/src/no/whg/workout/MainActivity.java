package no.whg.workout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class MainActivity extends FragmentActivity {
	public static boolean resetPressed;

	public static StrongLiftsCalculator SLCalc;
	String SLCalcFILENAME = "SLCALCOBJECT";
	
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    TextView testText;
    
	//variable for selection intent
	private final static int PICKER = 1;
	//variable to store the currently selected image
	private static int currentPic = 0;
	//adapter for gallery view
	private static PicAdapter imgAdapt;
	//gallery object
	private static Gallery picGallery;
	//image view for larger display
	private static ImageView picView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        FileInputStream fis = null;
        
		// Checking to see if the file with the object exists.
		try {
			fis = getApplicationContext().openFileInput(SLCalcFILENAME);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Inits SLCalc based on existence of file
		if (fis != null) {
			loadSLCalc(fis);
		} else{
			SLCalc = new StrongLiftsCalculator();
		}
        
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);
        imgAdapt = new PicAdapter(getApplicationContext());
        updateGallery();
        
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        getActionBar().setDisplayShowTitleEnabled(false);
        return true;
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		saveSLCalc();
	}

	@SuppressWarnings("deprecation")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if(item.getItemId() == R.id.menu_settings) {
    		startActivity(new Intent(this, SettingsActivity.class));
    	}
    	
    	if(item.getItemId() == R.id.menu_guide) {
    		startActivity(new Intent(this, GuideActivity.class));
    	}
    	
    	if (item.getItemId() == R.id.menu_camera){
    		Intent intent = new Intent(MainActivity.this, MediaCaptureActivity.class);
        	intent.putExtra("MEDIA_TYPE", 1);
        	intent.putExtra("method","yes");
        	startActivity(intent);
        	updateGallery();
    	}
    	
    	if(item.getItemId() == R.id.menu_music) {	//THIS DOES NOT WANT TO WORK ON 4.0
    		try {
    			Intent i = new Intent(Intent.ACTION_MAIN);
        		i.addCategory(Intent.CATEGORY_APP_MUSIC);
        		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(i);
    		} catch (Exception e){
    			try {
	        		Intent i = new Intent(Intent.ACTION_VIEW);
	        		i.setAction(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	        		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		startActivity(i);
    			} catch (Exception x) {
    				//TOAST
    			} 
    		}
    	}
    	
    	if(item.getItemId() == R.id.menu_help) {
    		DialogFragment dialog = new HelpDialog(mViewPager.getCurrentItem());
    		dialog.show(getFragmentManager(), "�");
    	}
    	
        return true;
        
    }
    
	// Loading the SLCalc object
	public void loadSLCalc(FileInputStream fis) {
		if (fis != null) {
			ObjectInputStream is = null;
			try {
				is = new ObjectInputStream(fis);
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StrongLiftsCalculator tempSL = null;
			try {
				tempSL = (StrongLiftsCalculator) is.readObject();
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SLCalc = tempSL;
		}
	}

	// Saving the SLCalc object
	public void saveSLCalc() {
		FileOutputStream fos = null;
		try {
			// fos sets up a file that is private, which means only this
			// application
			// can access it.
			fos = getApplicationContext().openFileOutput(SLCalcFILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.writeObject(SLCalc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	/* *
	 * Starts the MediaCaptureActivity with an intent. Used to record an exercise.
	 * 
	 * @param i	The parameter indicates what exercise this was called by
	 * @see MediaCaptureActivity
	 */
    public static void videoCapture(int i, Context c){
    	Exercise exercise = SLCalc.getBothSessions().get(i);
    	String lift = "SL_VID_";
    	Intent intent = new Intent(c,MediaCaptureActivity.class);
    	intent.putExtra("MEDIA_TYPE", 2);
    	intent.putExtra("method","yes");
    	
    	lift += exercise.getShortName();
		
    	intent.putExtra("lift", lift);

		c.startActivity(intent);
    }
    
    /* *
	 * Starts the MediaCaptureActivity with an intent. USed to view a recorded exercise.
	 * 
	 * @param i	The parameter indicates what exercise to view
	 * @see MediaCaptureActivity
	 */
    public static void videoPlay(int i, Context c){
    	Exercise exercise = SLCalc.getBothSessions().get(i);
    	String lift;
    	Intent intent = new Intent(c, MediaCaptureActivity.class);
    	intent.putExtra("MEDIA_TYPE", 3);
    	intent.putExtra("method","yes");
    	
    	lift = exercise.getShortName();
		
    	intent.putExtra("lift", lift);

		c.startActivity(intent);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(MainFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }
        
        @Override
        public int getItemPosition(Object object) {
    	   return POSITION_NONE;
    	}

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
                case 2: return getString(R.string.title_section3).toUpperCase();
                case 3: return getString(R.string.title_section4).toUpperCase();
            }
            return null;
        }
    }


    public static class MainFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        
        // STATS RELATED TEXTVIEWS
        public TextView tab3_tv_squats;
        public TextView tab3_tv_squats_deloads;
        public TextView tab3_tv_squats_fails;
        public TextView tab3_tv_benchPress;
        public TextView tab3_tv_benchPress_deloads;
        public TextView tab3_tv_benchPress_fails;
        public TextView tab3_tv_rowing;
        public TextView tab3_tv_rowing_deloads;
        public TextView tab3_tv_rowing_fails;
        public TextView tab3_tv_deadlift;
        public TextView tab3_tv_deadlift_deloads;
        public TextView tab3_tv_deadlift_fails;
        public TextView tab3_tv_OHP;
        public TextView tab3_tv_OHP_deloads;
        public TextView tab3_tv_OHP_fails;
        public LineGraphView graphView;
        public GraphViewSeries weightDataSeries;
        public LinearLayout layout;
        public ImageButton tab3_btn_video;
        public Button btn_squats;
        public Button btn_benchpress;
        public Button btn_rowing;
        public Button btn_deadlift;
        public Button btn_ohp;
        
        // LOG WORKOUT RELATED XML STUFF
		public LinearLayout tab1_ll_squats;
		public LinearLayout tab1_ll_benchpress;
		public LinearLayout tab1_ll_rowing;
		public LinearLayout tab1_ll_ohp;
		public LinearLayout tab1_ll_deadlift;
		
		public TextView tab1_tv_squatsTitle;
        public TextView tab1_tv_benchPressTitle;
        public TextView tab1_tv_rowingTitle;
        public TextView tab1_tv_deadliftTitle;
        public TextView tab1_tv_OHPTitle;
		
        public TextView tab1_tv_squats;
        public TextView tab1_tv_benchPress;
        public TextView tab1_tv_rowing;
        public TextView tab1_tv_deadlift;
        public TextView tab1_tv_OHP;
        
        // HOME PAGE VIEWS
        //public TextView tab2_tv_aOrB;
        //public TextView tab2_tv_exerciseOneWeight;
        public RelativeLayout tab2_squats_rl;
        public TextView tab2_squats_sets;
        public TextView tab2_squats_weight;
        public RelativeLayout tab2_bench_rl;
        public TextView tab2_bench_sets;
        public TextView tab2_bench_weight;
        public RelativeLayout tab2_deadlift_rl;
        public TextView tab2_deadlift_sets;
        public TextView tab2_deadlift_weight;
        public RelativeLayout tab2_ohp_rl;
        public TextView tab2_ohp_sets;
        public TextView tab2_ohp_weight;
        public RelativeLayout tab2_rowing_rl;
        public TextView tab2_rowing_sets;
        public TextView tab2_rowing_weight;
        
        List<ThreeStateCheckbox> tab1_squats = new ArrayList<ThreeStateCheckbox>(5);
        List<ThreeStateCheckbox> tab1_benchpress = new ArrayList<ThreeStateCheckbox>(5);
        List<ThreeStateCheckbox> tab1_rowing = new ArrayList<ThreeStateCheckbox>(5);
        List<ThreeStateCheckbox> tab1_ohp = new ArrayList<ThreeStateCheckbox>(5);
        List<ThreeStateCheckbox> tab1_deadlift = new ArrayList<ThreeStateCheckbox>(5);
        
        public Button tab1_b_log;
        public String weightUnit;
        
        public ImageButton tab1_b_squats;
        public ImageButton tab1_b_benchpress;
        public ImageButton tab1_b_rowing;
        public ImageButton tab1_b_deadlift;
        public ImageButton tab1_b_ohp;
        
        public List<Exercise> currentSession = SLCalc.getCurrentSession();
		public List<Exercise> bothSessions = SLCalc.getBothSessions();
		
		final boolean[] completed = new boolean[4];
        
        
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	Bundle args = getArguments();
        	int position = args.getInt(ARG_SECTION_NUMBER);
        	int tabLayout = 1;

        	switch(position) {
        	case 0:
        		tabLayout = R.layout.tab1;
        		break;
        	case 1:
        		tabLayout = R.layout.tab2;
        		break;
        	case 2:
        		tabLayout = R.layout.tab3;
        		break;
        	case 3:
        		tabLayout = R.layout.tab4;
        		break;
        		
        	}
        	
        	View view = inflater.inflate(tabLayout, container, false);

            return view;
        }

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			
        	Bundle args = getArguments();
        	int position = args.getInt(ARG_SECTION_NUMBER);

        	switch(position) {
        	case 0:
        		// Tab 1 - Log Workout
    			initTab1();
    			refreshTab1();
        		break;
        	case 1:
        		// Tab 2 - Home
        		initTab2();
        		break;
        	case 2:
        		// Tab 3 - Stats
        		// Initializing the TextViews.
        		initTab3();
        		refreshTab3();
        		break;
        	case 3:
        		// Tab 4 - Gallery
        		initTab4();
        		
        		currentPic = 0;

        		/* *
        		 * Sets up a LongClickListener for replacing gallery pictures.
        		 * 
        		 * @return true	Returns true upon completion.
        		 */
          		picGallery.setOnItemLongClickListener(new OnItemLongClickListener() {
          			//handle long clicks
          			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
          				//update the currently selected position so that we assign the imported bitmap to correct item
          				currentPic = position;
          				//take the user to their chosen image selection app (gallery or file manager)
          				Intent pickIntent = new Intent();
          				pickIntent.setType("image/*");
          				pickIntent.setAction(Intent.ACTION_GET_CONTENT);
          				//handle the returned data in onActivityResult
          				startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKER);
          				return true;
          			}
          		});
          		
          		/* *
        		 * Sets up a ClickListener for choosing gallery pictures.
        		 * 
        		 */
          		picGallery.setOnItemClickListener(new OnItemClickListener() {
          			//handle clicks
          			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          				//set the larger image view to display the chosen bitmap calling method of adapter class
          				Matrix matrix = new Matrix();
    					matrix.setRotate(90);
    					picView.setImageBitmap(Bitmap.createBitmap(imgAdapt.getPic(position), 0, 0, imgAdapt.getPic(position).getWidth(), imgAdapt.getPic(position).getHeight(), matrix, false));
          				currentPic = position;
          			}
          		});
    	        break;
        	}
		}
		
		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			
			Bundle args = getArguments();
			int position = args.getInt(ARG_SECTION_NUMBER);
			
			switch(position) {
			case 0:
				// Tab 1 - Log Workout
				refreshTab1();
				break;
			case 1:
				initTab2();
				break;
			case 2:
				// Tab 3 - Stats
				refreshTab3();
				break;
			case 3:
				// Tab 4 - Gallery
				initTab4();
				//refreshGallery();
				break;
			}
		}
		
		@Override
		public void onPause() {
			// TODO Auto-generated method stub
			super.onPause();
			
			Bundle args = getArguments();
			int position = args.getInt(ARG_SECTION_NUMBER);
			
			switch(position) {
			case 0:
				// Tab 1 - Log Workout
				break;
			case 1:
				// Tab 2 - Home
				break;
			case 2:
				// Tab 3 - Stats
				break;
			case 3:
				// Tab 4 - Gallery
				break;
			}
		}
		
		@Override
		public void onStop() {
			// TODO Auto-generated method stub
			super.onStop();
			
			Bundle args = getArguments();
			int position = args.getInt(ARG_SECTION_NUMBER);
			
			switch(position) {
			case 0:
				// Tab 1 - Log Workout
				break;
			case 1:
				// Tab 2 - Home
				break;
			case 2:
				// Tab 3 - Stats
				break;
			case 3:
				// Tab 4 - Gallery
				break;
			}
		}
		
		//Initializes tab 1
		// BEING WORLD CHAMPIONSHIP OF LAZY CODE ~~
		public void initTab1() {
			tab1_tv_squats 			= (TextView) getActivity().findViewById(R.id.log_squatsDetailed);
			tab1_tv_benchPress 		= (TextView) getActivity().findViewById(R.id.log_benchPressDetailed);
			tab1_tv_rowing 			= (TextView) getActivity().findViewById(R.id.log_rowingDetailed);
			tab1_tv_deadlift 		= (TextView) getActivity().findViewById(R.id.log_deadliftDetailed);
			tab1_tv_OHP 			= (TextView) getActivity().findViewById(R.id.log_ohpDetailed);
			
			tab1_ll_deadlift 		= (LinearLayout) getActivity().findViewById(R.id.log_linearFour);
			tab1_ll_benchpress 		= (LinearLayout) getActivity().findViewById(R.id.log_linearTwo);
			tab1_ll_rowing 			= (LinearLayout) getActivity().findViewById(R.id.log_linearThree);
			tab1_ll_ohp 			= (LinearLayout) getActivity().findViewById(R.id.log_linearFive);

			tab1_tv_squatsTitle 	= (TextView) getActivity().findViewById(R.id.log_squats);
			tab1_tv_benchPressTitle = (TextView) getActivity().findViewById(R.id.log_benchPress);
			tab1_tv_rowingTitle		= (TextView) getActivity().findViewById(R.id.log_rowing);
			tab1_tv_deadliftTitle	= (TextView) getActivity().findViewById(R.id.log_deadlift);
			tab1_tv_OHPTitle		= (TextView) getActivity().findViewById(R.id.log_ohp);
			
			tab1_b_squats			= (ImageButton) getActivity().findViewById(R.id.log_squatsVideo);
			tab1_b_benchpress		= (ImageButton) getActivity().findViewById(R.id.log_benchpressVideo);
			tab1_b_rowing			= (ImageButton) getActivity().findViewById(R.id.log_rowingVideo);
			tab1_b_deadlift			= (ImageButton) getActivity().findViewById(R.id.log_deadliftVideo);
			tab1_b_ohp				= (ImageButton) getActivity().findViewById(R.id.log_ohpVideo);
			
			tab1_b_log				= (Button) getActivity().findViewById(R.id.log_button);
			

			for(int i = 0; i < 5; i++) { // counting from 1, fml..
				String squatsId = "log_squats_cb"+i;
				int squatsIdInt = getResources().getIdentifier(squatsId, "id", "no.whg.workout");
				tab1_squats.add(i, (ThreeStateCheckbox) getActivity().findViewById(squatsIdInt));
				
				String benchpressId = "log_benchPress_cb"+i;
				int benchpressIdInt = getResources().getIdentifier(benchpressId, "id", "no.whg.workout");
				tab1_benchpress.add(i, (ThreeStateCheckbox) getActivity().findViewById(benchpressIdInt));
				
				String rowingId = "log_rowing_cb"+i;
				int rowingIdInt = getResources().getIdentifier(rowingId, "id", "no.whg.workout");
				tab1_rowing.add(i,(ThreeStateCheckbox) getActivity().findViewById(rowingIdInt));
				
				String ohpId = "log_ohp_cb"+i;
				int ohpIdInt = getResources().getIdentifier(ohpId, "id", "no.whg.workout");
				tab1_ohp.add(i,(ThreeStateCheckbox) getActivity().findViewById(ohpIdInt));
				
				String deadliftId = "log_deadlift_cb"+i;
				int deadliftIdInt = getResources().getIdentifier(deadliftId, "id", "no.whg.workout");
				tab1_deadlift.add(i, (ThreeStateCheckbox) getActivity().findViewById(deadliftIdInt));
				
			}
		}
		
		public void refreshTab1() {
			
			completed[0] = false;
			completed[1] = false;
			completed[2] = false;

			boolean isA = SLCalc.getSessionTypeA();
			@SuppressWarnings("deprecation")
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
			        ViewGroup.LayoutParams.WRAP_CONTENT);
			
			p.setMargins(15, 15, 15, 15); // left, top, right, bottom
			
			
			// Number in list -> exercise:
			// 0 - Squats
			// 1 - Benchpress
			// 2 - Rowing
			// 3 - OHP
			// 4 - Deadlift
	        
	        setWeightString();

			if(isA) {
				tab1_tv_squats.setText(String.valueOf(currentSession.get(0).getCurrentWeight()) + weightUnit);
				tab1_tv_benchPress.setText(String.valueOf(currentSession.get(1).getCurrentWeight()) + weightUnit);
				tab1_tv_rowing.setText(String.valueOf(currentSession.get(2).getCurrentWeight()) + weightUnit);				
				
				tab1_ll_deadlift.setVisibility(View.GONE);
				tab1_tv_deadliftTitle.setVisibility(View.GONE);
				tab1_b_deadlift.setVisibility(View.GONE);
				tab1_ll_ohp.setVisibility(View.GONE);
				tab1_tv_OHPTitle.setVisibility(View.GONE);
				tab1_b_ohp.setVisibility(View.GONE);
				

				
				if(bothSessions.get(0).getNumberOfSets() == 1) {
					
					tab1_squats.get(1).setVisibility(View.GONE);
					tab1_squats.get(2).setVisibility(View.GONE);
					tab1_squats.get(3).setVisibility(View.GONE);
					tab1_squats.get(4).setVisibility(View.GONE);
	
				}
				
				if(bothSessions.get(1).getNumberOfSets() == 1) {
					tab1_deadlift.get(1).setVisibility(View.GONE);
					tab1_deadlift.get(2).setVisibility(View.GONE);
					tab1_deadlift.get(3).setVisibility(View.GONE);
					tab1_deadlift.get(4).setVisibility(View.GONE);
					
				}
				
				if(bothSessions.get(2).getNumberOfSets() == 1) {
					tab1_rowing.get(1).setVisibility(View.GONE);
					tab1_rowing.get(2).setVisibility(View.GONE);
					tab1_rowing.get(3).setVisibility(View.GONE);
					tab1_rowing.get(4).setVisibility(View.GONE);
					
				}
				
				
				
				if(bothSessions.get(0).getNumberOfSets() == 3) {
					tab1_squats.get(3).setVisibility(View.GONE);
					tab1_squats.get(4).setVisibility(View.GONE);
				}
				if(bothSessions.get(1).getNumberOfSets() == 3) {
					tab1_benchpress.get(3).setVisibility(View.GONE);
					tab1_benchpress.get(4).setVisibility(View.GONE);
				}
				if(bothSessions.get(2).getNumberOfSets() == 3) {
					tab1_rowing.get(3).setVisibility(View.GONE);
					tab1_rowing.get(4).setVisibility(View.GONE);
				}
				

				
				
				p.addRule(RelativeLayout.BELOW, R.id.log_linearThree);
								
				tab1_b_log.setLayoutParams(p);

				for(int i = 0; i < currentSession.get(0).getNumberOfSets(); i++) { 
					
			        tab1_squats.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
			        		int[] state = new int[5];
							
							List<Exercise> currentSession = SLCalc.getCurrentSession();

			        		for(int j = 0; j < currentSession.get(0).getNumberOfSets(); j++) {
				        		state[j] = tab1_squats.get(j).getState();
				        		
			        		}
			        		if(currentSession.get(0).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(0).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							} else if(currentSession.get(0).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							}

			        	}
			        });
			        
			        tab1_benchpress.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
			        		int[] state = new int[5];
							
							List<Exercise> currentSession = SLCalc.getCurrentSession();
			        		for(int j = 0; j < currentSession.get(0).getNumberOfSets(); j++) {
				        		state[j] = tab1_benchpress.get(j).getState();
			        		}
							if(currentSession.get(1).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[1] = true;
									System.out.println("BENCHPRESS COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("BENCHPRESS COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(1).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[1] = true;
									System.out.println("BENCHPRESS COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("BENCHPRESS COMPLETED = FALSE");
								}
							} else if(currentSession.get(1).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[1] = true;
									System.out.println("BENCHPRESS COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("BENCHPRESS COMPLETED = FALSE");
								}
							}
			        		}

			        });
			        
			        tab1_rowing.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
			        		int[] state = new int[5];
							
							List<Exercise> currentSession = SLCalc.getCurrentSession();
			        		for(int j = 0; j < currentSession.get(0).getNumberOfSets(); j++) {
				        		state[j] = tab1_rowing.get(j).getState();
			        		}
			        		if(currentSession.get(2).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[2] = true;
									System.out.println("ROWING COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("ROWING COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(2).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[2] = true;
									System.out.println("ROWING COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("ROWING COMPLETED = FALSE");
								}
							} else if(currentSession.get(2).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[2] = true;
									System.out.println("ROWING COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("ROWING COMPLETED = FALSE");
								}
							}
			        		}

			        });
			        

					
				}
				
				// send stuff to slcalc here

			} else  {
				if(bothSessions.get(0).getNumberOfSets() == 3) {
					tab1_squats.get(3).setVisibility(View.GONE);
					tab1_squats.get(4).setVisibility(View.GONE);
				}
				
				if(bothSessions.get(0).getNumberOfSets() == 1) {
					
					tab1_squats.get(1).setVisibility(View.GONE);
					tab1_squats.get(2).setVisibility(View.GONE);
					tab1_squats.get(3).setVisibility(View.GONE);
					tab1_squats.get(4).setVisibility(View.GONE);
	
				}
				
				if(bothSessions.get(3).getNumberOfSets() == 1) {
					tab1_ohp.get(1).setVisibility(View.GONE);
					tab1_ohp.get(2).setVisibility(View.GONE);
					tab1_ohp.get(3).setVisibility(View.GONE);
					tab1_ohp.get(4).setVisibility(View.GONE);
					
				}
				
				if(bothSessions.get(4).getNumberOfSets() == 1) {
					tab1_deadlift.get(1).setVisibility(View.GONE);
					tab1_deadlift.get(2).setVisibility(View.GONE);
					tab1_deadlift.get(3).setVisibility(View.GONE);
					tab1_deadlift.get(4).setVisibility(View.GONE);
					System.out.println("I AM RUNNING");
					
					
				}
				
				if(bothSessions.get(3).getNumberOfSets() == 3) {
					tab1_ohp.get(3).setVisibility(View.GONE);
					tab1_ohp.get(4).setVisibility(View.GONE);
				}
				if(bothSessions.get(4).getNumberOfSets() == 3) {
					tab1_deadlift.get(3).setVisibility(View.GONE);
					tab1_deadlift.get(4).setVisibility(View.GONE);
				}
				
				tab1_tv_squats.setText(String.valueOf(currentSession.get(0).getCurrentWeight()) + weightUnit);
				tab1_tv_OHP.setText(String.valueOf(currentSession.get(1).getCurrentWeight()) + weightUnit);	
				tab1_tv_deadlift.setText(String.valueOf(currentSession.get(2).getCurrentWeight()) + weightUnit);
				
				tab1_ll_benchpress.setVisibility(View.GONE);
				tab1_tv_benchPressTitle.setVisibility(View.GONE);
				tab1_b_benchpress.setVisibility(View.GONE);
				tab1_ll_rowing.setVisibility(View.GONE);
				tab1_tv_rowingTitle.setVisibility(View.GONE);
				tab1_b_rowing.setVisibility(View.GONE);
				
				
				p.addRule(RelativeLayout.BELOW, R.id.log_linearFive);
				
				tab1_b_log.setLayoutParams(p);
				
				System.out.println(currentSession.get(0).getNumberOfSets());
				
				for(int i = 0; i < currentSession.get(0).getNumberOfSets(); i++) { 
			        tab1_squats.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
							int[] state = new int[5];
							
							List<Exercise> currentSession = SLCalc.getCurrentSession();

							for(int j = 0; j < currentSession.get(0).getNumberOfSets(); j++) {
								state[j] = tab1_squats.get(j).getState();
								System.out.println("State " + j + ": " + state[j]);
							}
							if(currentSession.get(0).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(0).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							} else if(currentSession.get(0).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[0] = true;
									System.out.println("SQUATS COMPLETED = TRUE");
								} else {
									completed[0] = false;
									System.out.println("SQUATS COMPLETED = FALSE");
								}
							}
							
									
							


			        	}
			        });
			        tab1_ohp.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
			        		
							int[] state = new int[5];
							List<Exercise> currentSession = SLCalc.getCurrentSession();
			        		for(int j = 0; j < currentSession.get(0).getNumberOfSets(); j++) {
				        		state[j] = tab1_ohp.get(j).getState();

			        		}
							if(currentSession.get(1).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[1] = true;
									System.out.println("OHP COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("OHP COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(1).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[1] = true;
									System.out.println("OHP COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("OHP COMPLETED = FALSE");
								}
							} else if(currentSession.get(1).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[1] = true;
									System.out.println("OHP COMPLETED = TRUE");
								} else {
									completed[1] = false;
									System.out.println("OHP COMPLETED = FALSE");
								}
							}

			        	}
			        });
			        
			        tab1_deadlift.get(i).setOnClickListener(new View.OnClickListener() {
			        	public void onClick(View v) {
			        		int[] state = new int[5];
							List<Exercise> currentSession = SLCalc.getCurrentSession();
			        		for(int j = 0; j <= 4; j++) {
				        		state[j] = tab1_deadlift.get(j).getState();
				        		}
			        		if(currentSession.get(2).getNumberOfSets() == 3) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1) {
									completed[2] = true;
									System.out.println("DEADLIFT COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("DEADLIFT COMPLETED = FALSE");
								}
							
							} else if(currentSession.get(2).getNumberOfSets() == 1) {
								if(state[0] == 1) {
									completed[2] = true;
									System.out.println("DEADLIFT COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("DEADLIFT COMPLETED = FALSE");
								}
							} else if(currentSession.get(2).getNumberOfSets() == 5) {
								if(state[0] == 1 && state[1] == 1 && state[2] == 1 && state[3] == 1 && state[4] == 1) {
									completed[2] = true;
									System.out.println("DEADLIFT COMPLETED = TRUE");
								} else {
									completed[2] = false;
									System.out.println("DEADLIFT COMPLETED = FALSE");
								}
							}


			        	}
			        });
			        
				}
				

			}
			

			


			tab1_b_log.setOnClickListener(new OnClickListener() {
			    public void onClick(View v)
			    {
			    	updateSuccess(completed[0], completed[1], completed[2]);

			    } 
			});
			
			tab1_b_squats.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Context c = getActivity();
					videoCapture(0, c);
					
				}
			});
			
			tab1_b_benchpress.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Context c = getActivity();
					videoCapture(1, c);
				}
			});
			
			tab1_b_rowing.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Context c = getActivity();
					videoCapture(2, c);
				}
			});
			
			tab1_b_deadlift.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Context c = getActivity();
					videoCapture(3, c);
				}
			});
			
			tab1_b_ohp.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Context c = getActivity();
					videoCapture(4, c);
				}
			});
		}
		
		public void updateSuccess(boolean completed1, boolean completed2, boolean completed3) { // Sends results to exercise
			System.out.println("Completed[0]: " + completed[0] + "\n" + "Completed[1]: " + completed[1] + "\n" + "Completed[2]: " + completed[2]);

			String title = getResources().getString(R.string.alert_note);
			String confirm = getResources().getString(android.R.string.ok);
			String cancel = getResources().getString(android.R.string.cancel);
			String youSure = getResources().getString(R.string.alert_sure);
			
			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setTitle(title);
			alertDialog.setMessage(youSure);
			alertDialog.setPositiveButton(confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(completed[0]) {
								
								currentSession.get(0).setSuccess(completed[0]);
							}
							
							if(completed[1]) {
								currentSession.get(1).setSuccess(completed[1]);

							}
							
							if(completed[2]) {
								currentSession.get(2).setSuccess(completed[2]);

							}
							
							SLCalc.updateSessionWeights(currentSession);
							
							refreshTab1();
							
							//ViewGroup vg = (ViewGroup) getActivity().findViewById(R.id.tab1);
							//vg.invalidate();
						}
					});

			alertDialog.setNegativeButton(cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.show();
			
			
		}
		// END WORLD CHAMPIONSHIP OF LAZY CODE ~~
			

		
		// Initializes tab 2
		public void initTab2() {
			String w;
			if (SLCalc.getWeightUnitKilograms()){
				w = " KG";
			} else {
				w = " lbs";
			}
			if (SLCalc.getSessionTypeA()) {
				List<Exercise> e = SLCalc.getSessionByName("A");

				tab2_deadlift_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_deadlift);
				tab2_ohp_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_ohp);
				tab2_deadlift_rl.setVisibility(View.GONE);
				tab2_ohp_rl.setVisibility(View.GONE);
				
				tab2_squats_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_squats);
				tab2_squats_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_squats_sets);
				tab2_squats_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_squats_weight);
				tab2_bench_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_rowing);
				tab2_bench_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_bench_sets);
				tab2_bench_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_bench_weight);
				tab2_rowing_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_rowing);
				tab2_rowing_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_rowing_sets);
				tab2_rowing_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_rowing_weight);
				
				System.out.println("0: " + e.get(0).getName());
				System.out.println("1: " + e.get(1).getName());
				System.out.println("2: " + e.get(2).getName());
				
				tab2_squats_sets.setText(String.valueOf(e.get(0).getNumberOfSets()));
				tab2_squats_weight.setText(String.valueOf(e.get(0).getCurrentWeight()) + w);
				tab2_bench_sets.setText(String.valueOf(e.get(1).getNumberOfSets()));
				tab2_bench_weight.setText(String.valueOf(e.get(1).getCurrentWeight()) + w);
				tab2_rowing_sets.setText(String.valueOf(e.get(2).getNumberOfSets()));
				tab2_rowing_weight.setText(String.valueOf(e.get(2).getCurrentWeight()) + w);
				tab2_squats_rl.setVisibility(View.VISIBLE);
				tab2_bench_rl.setVisibility(View.VISIBLE);
				tab2_rowing_rl.setVisibility(View.VISIBLE);
			} else {
				List<Exercise> e = SLCalc.getSessionByName("B");
				
				tab2_bench_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_bench);
				tab2_rowing_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_rowing);
				tab2_bench_rl.setVisibility(View.GONE);
				tab2_rowing_rl.setVisibility(View.GONE);
				
				tab2_squats_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_squats);
				tab2_squats_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_squats_sets);
				tab2_squats_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_squats_weight);
				tab2_deadlift_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_deadlift);
				tab2_deadlift_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_deadlift_sets);
				tab2_deadlift_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_deadlift_weight);
				tab2_ohp_rl = (RelativeLayout) getActivity().findViewById(R.id.tab2_ohp);
				tab2_ohp_sets = (TextView) getActivity().findViewById(R.id.front_tv_workout_ohp_sets);
				tab2_ohp_weight = (TextView) getActivity().findViewById(R.id.front_tv_workout_ohp_weight);


				System.out.println("0: " + e.get(0).getName());
				System.out.println("1: " + e.get(1).getName());
				System.out.println("2: " + e.get(2).getName());
				
				tab2_squats_sets.setText(String.valueOf(e.get(0).getNumberOfSets()));
				tab2_squats_weight.setText(String.valueOf(e.get(0).getCurrentWeight()) + w);
				tab2_ohp_sets.setText(String.valueOf(e.get(1).getNumberOfSets()));
				tab2_ohp_weight.setText(String.valueOf(e.get(1).getCurrentWeight()) + w);
				tab2_deadlift_sets.setText(String.valueOf(e.get(2).getNumberOfSets()));
				tab2_deadlift_weight.setText(String.valueOf(e.get(2).getCurrentWeight()) + w);
				tab2_squats_rl.setVisibility(View.VISIBLE);
				tab2_deadlift_rl.setVisibility(View.VISIBLE);
				tab2_ohp_rl.setVisibility(View.VISIBLE);
			}
		}       		
			
		//Initializes tab 3
		public void initTab3(){
//			layout 						= (LinearLayout) getActivity().findViewById(R.id.stats_graphViewLayout); 
			tab3_tv_squats 				= (TextView) getActivity().findViewById(R.id.stats_squatsDetailed);
			tab3_tv_squats_deloads 		= (TextView) getActivity().findViewById(R.id.stats_squatsDetailed2);
			tab3_tv_squats_fails 		= (TextView) getActivity().findViewById(R.id.stats_squatsDetailed3);
			tab3_tv_benchPress 			= (TextView) getActivity().findViewById(R.id.stats_benchPressDetailed);
			tab3_tv_benchPress_deloads 	= (TextView) getActivity().findViewById(R.id.stats_benchPressDetailed2);
			tab3_tv_benchPress_fails 	= (TextView) getActivity().findViewById(R.id.stats_benchPressDetailed3);
			tab3_tv_rowing 				= (TextView) getActivity().findViewById(R.id.stats_rowingDetailed);
			tab3_tv_rowing_deloads 		= (TextView) getActivity().findViewById(R.id.stats_rowingDetailed2);
			tab3_tv_rowing_fails 		= (TextView) getActivity().findViewById(R.id.stats_rowingDetailed3);
			tab3_tv_deadlift 			= (TextView) getActivity().findViewById(R.id.stats_deadliftDetailed);
			tab3_tv_deadlift_deloads 	= (TextView) getActivity().findViewById(R.id.stats_deadliftDetailed2);
			tab3_tv_deadlift_fails 		= (TextView) getActivity().findViewById(R.id.stats_deadliftDetailed3);
			tab3_tv_OHP 				= (TextView) getActivity().findViewById(R.id.stats_ohpDetailed);
			tab3_tv_OHP_deloads 		= (TextView) getActivity().findViewById(R.id.stats_ohpDetailed2);
			tab3_tv_OHP_fails 			= (TextView) getActivity().findViewById(R.id.stats_ohpDetailed3);
			tab3_btn_video				= (ImageButton) getActivity().findViewById(R.id.stats_videoBtn);
//			graphView					= new LineGraphView(getActivity().getApplicationContext(), "Squats graph");
//			layout.addView(graphView);
			
		}
		
		public void refreshTab3(){
			List<Exercise> 	exercises;
			exercises = SLCalc.getBothSessions();
			
			setWeightString();
			
			// Number in list -> exercise:
			// 0 - Squats
			// 1 - Benchpress
			// 2 - Rowing
			// 3 - OHP
			// 4 - Deadlift

			tab3_tv_squats.setText(String.valueOf(exercises.get(0).getCurrentWeight()) + weightUnit);
			tab3_tv_squats_deloads.setText("Deloads: " + String.valueOf(exercises.get(0).getNumberOfDeloads()));
			tab3_tv_squats_fails.setText("Fails: " + String.valueOf(exercises.get(0).getNumberOfFails()));
			tab3_tv_benchPress.setText(String.valueOf(exercises.get(1).getCurrentWeight()) + weightUnit);
			tab3_tv_benchPress_deloads.setText("Deloads: " + String.valueOf(exercises.get(1).getNumberOfDeloads()));
			tab3_tv_benchPress_fails.setText("Fails: " + String.valueOf(exercises.get(1).getNumberOfFails()));
			tab3_tv_rowing.setText(String.valueOf(exercises.get(2).getCurrentWeight()) + weightUnit);
			tab3_tv_rowing_deloads.setText("Deloads: " + String.valueOf(exercises.get(2).getNumberOfDeloads()));
			tab3_tv_rowing_fails.setText("Fails: " + String.valueOf(exercises.get(2).getNumberOfFails()));
			tab3_tv_deadlift.setText(String.valueOf(exercises.get(4).getCurrentWeight()) + weightUnit);
			tab3_tv_deadlift_deloads.setText("Deloads: " + String.valueOf(exercises.get(4).getNumberOfDeloads()));
			tab3_tv_deadlift_fails.setText("Fails: " + String.valueOf(exercises.get(4).getNumberOfFails()));
			tab3_tv_OHP.setText(String.valueOf(exercises.get(3).getCurrentWeight()) + weightUnit);
			tab3_tv_OHP_deloads.setText("Deloads: " + String.valueOf(exercises.get(3).getNumberOfDeloads()));
			tab3_tv_OHP_fails.setText("Fails: " + String.valueOf(exercises.get(3).getNumberOfFails()));
						
			tab3_btn_video.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					final Dialog dialog = new Dialog(getActivity());
					dialog.setContentView(R.layout.custom_video_list);
					dialog.setTitle("Videos");
					
					btn_squats 					= (Button) dialog.findViewById(R.id.list_squatBtn);
					btn_benchpress 				= (Button) dialog.findViewById(R.id.list_benchpressBtn);
					btn_rowing					= (Button) dialog.findViewById(R.id.list_rowingBtn);
					btn_deadlift				= (Button) dialog.findViewById(R.id.list_deadliftBtn);
					btn_ohp						= (Button) dialog.findViewById(R.id.list_ohpBtn);
					
					btn_squats.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
								// TODO Auto-generated method stub
								Context c = getActivity();
								videoPlay(0, c);
							}
					});
					
					btn_squats.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Context c = getActivity();
							videoPlay(0, c);
						}
					});
					
					btn_benchpress.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Context c = getActivity();
							videoPlay(1, c);
						}
					});
					
					btn_rowing.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Context c = getActivity();
							videoPlay(2, c);
						}
					});
					
					btn_deadlift.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Context c = getActivity();
							videoPlay(3, c);
						}
					});
					
					btn_ohp.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Context c = getActivity();
							videoPlay(4, c);
						}
					});
					
					dialog.show();
				}
			});
			
//			populateGraph();
		}
		
//		@SuppressWarnings("deprecation")
//		public void populateGraph(){
//			List<Double> weightData;
//			weightData = SLCalc.getBothSessions().get(0).getProgressList(); 
//			TextView tv_noData = (TextView) getActivity().findViewById(R.id.stats_tvNoData);
//			GraphViewData[] graphViewData;
//			
//			// Only populates the graph if the progresslist has data in it
//			if (!weightData.isEmpty()) {
//				
//				tv_noData.setVisibility(View.GONE);
//				graphView.setVisibility(View.VISIBLE);
//
////				graphViewData = new GraphViewData[weightData.size()];
//				
//				GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {  
//					      new GraphViewData(1, 2.0d)  
//					      , new GraphViewData(2, 1.5d)  
//					      , new GraphViewData(3, 2.5d)  
//					      , new GraphViewData(4, 1.0d)  
//					});  
//				
////				for (int i = 0; i < weightData.size(); i++) {
////					graphViewData[i] = new GraphViewData(i, (double)weightData.get(i));
////					System.out.println(i);
////				}
//				
//				// Inits and resets the weightDataSeries
////				weightDataSeries = new GraphViewSeries(graphViewData);
//				
//				graphView.addSeries(exampleSeries);
//			} else {
//				tv_noData.setVisibility(View.VISIBLE);
//				graphView.setVisibility(View.GONE);
//				tv_noData.setText("No data to display");
//			    tv_noData.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//			}
//		}
//		
		//Initializes tab 4
		public void initTab4(){
			//get the large image view
      		picView = (ImageView) getActivity().findViewById(R.id.tab4_picture);
      		//get the gallery view
      		picGallery = (Gallery) getActivity().findViewById(R.id.tab4_gallery);
      		//set the imgadapter for picgallery
            picGallery.setAdapter(imgAdapt);
            //redraw the gallery thumbnails to reflect the new addition
			picGallery.setAdapter(imgAdapt);
			//display the newly selected image at larger size
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			Bitmap pic = imgAdapt.getPic(currentPic);
			picView.setImageBitmap(Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), matrix, false));
			picView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		
		public void setWeightString(){
			if (SLCalc.getWeightUnitKilograms())
				weightUnit  = " KG";
			else
				weightUnit = " Lbs";
		}
    }
    
    /* *
	 * Base Adapter subclass creates Gallery view
	 * - provides methods for adding new images from user selection
	 * - provides method to return bitmaps from array
	 *
	 */
	public class PicAdapter extends BaseAdapter {

		//use the default gallery background image
		int defaultItemBackground;
		//gallery context
		private Context galleryContext;

		//array to store bitmaps to display
		private Bitmap[] imageBitmaps;
		//placeholder bitmap for empty spaces in gallery
		Bitmap placeholder;

		//constructor
		public PicAdapter(Context c) {
			//instantiate context
			galleryContext = getApplicationContext();
				
			//create bitmap array
			imageBitmaps = new Bitmap[10];
			//decode the placeholder image
			placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			Matrix matrix = new Matrix();
			matrix.setRotate(-90);
			placeholder = Bitmap.createBitmap(placeholder, 0, 0, placeholder.getWidth(), placeholder.getHeight(), matrix, false);

			//set placeholder as all thumbnail images in the gallery initially
			for(int i=0; i<imageBitmaps.length; i++)
				imageBitmaps[i]=placeholder;

			//get the styling attributes - use default Andorid system resources
			TypedArray styleAttrs = galleryContext.obtainStyledAttributes(R.styleable.PicGallery);
			//get the background resource
			defaultItemBackground = styleAttrs.getResourceId(R.styleable.PicGallery_android_galleryItemBackground, 0);
			//defaultItemBackground = R.drawable.splash;
			//recycle attributes
			styleAttrs.recycle();
		}

		//BaseAdapter methods

		//return number of data items i.e. bitmap images
		public int getCount() {
			return imageBitmaps.length;
		}

		//return item at specified position
		public Object getItem(int position) {
			return position;
		}

		//return item ID at specified position
		public long getItemId(int position) {
			return position;
		}

		//get view specifies layout and display options for each thumbnail in the gallery
		public View getView(int position, View convertView, ViewGroup parent) {

			//create the view
			ImageView imageView = new ImageView(galleryContext);
			//specify the bitmap at this position in the array
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			imageView.setImageBitmap(Bitmap.createBitmap(imageBitmaps[position], 0, 0, imageBitmaps[position].getWidth(), imageBitmaps[position].getHeight(), matrix, false));
			//set layout options
			imageView.setLayoutParams(new Gallery.LayoutParams(230, 300));
			//scale type within view area
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			//set default gallery item background
			imageView.setBackgroundResource(defaultItemBackground);
			//return the view
			return imageView;
		}

		//custom methods for this app

		//helper method to add a bitmap to the gallery when the user chooses one
		public void addPic(Bitmap newPic)
		{
			//set at currently selected index
			imageBitmaps[currentPic] = newPic;
		}

		//helper method to add a bitmap to the gallery programatically
		public void addPic (Bitmap newPic, int i)
		{
			//set at requested index
			imageBitmaps[i] = newPic;
		}
		
		public void addNewPic (Bitmap newPic)
		{
			//set at requested index
			//Bitmap[] tempBitmaps = imageBitmaps;
			for (int i = imageBitmaps.length - 1; i > 1; i--){
				imageBitmaps[i] = imageBitmaps[i-1];
			}
			imageBitmaps[0] = newPic;
		}

		//return bitmap at specified position for larger display
		public Bitmap getPic(int pos)
		{
			//return bitmap at pos index
			return imageBitmaps[pos];
		}
		
		public void resetBitmapArray(){
			//recycle all thumbnail images in the gallery
			for(int i=0; i<imageBitmaps.length; i++)
				imageBitmaps[i].recycle();
		}
	}
	

	/* *
	 * Updates the content of the gallery
	 * 
	 */
	protected void updateGallery(){
		File dir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						"StrongLifts"); // set destination folder
		
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < 10; i++){
				//the returned picture URI
				Uri imgUri = Uri.fromFile(files[files.length - i - 1]);
				if(imgUri!=null) {
					String imgPath = imgUri.getPath();
				    class MyThread implements Runnable {
						String imgPath;
						int pos;
						public MyThread (String s, int pos) {
							this.imgPath = s;
							this.pos = pos;
						}
						public void run (){
							Bitmap bitmap = decodeSampledBitmapFromPath(imgPath);
							imgAdapt.addPic(bitmap, pos);
							invalidator();
						}
					}
					Runnable r = new MyThread(imgPath, i);
					new Thread(r).start();
				}
			}
		}
	}
	
	/**
	 * Handles returning from gallery or file manager image selection and imports the image bitmap.
	 * 
	 */
	@SuppressWarnings("deprecation")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {
			//the returned picture URI
			Uri imgUri = data.getData();
			
			//declare the path string
			String imgPath = "";

			//retrieve the string using media data
			String[] medData = { MediaStore.Images.Media.DATA };
			//query the data
			Cursor picCursor = managedQuery(imgUri, medData, null, null, null);
			if(picCursor!=null)
			{
				//get the path string
				int index = picCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				picCursor.moveToFirst();
				imgPath = picCursor.getString(index);
			}
			else
				imgPath = imgUri.getPath();
			
			View vg = (View)findViewById (R.id.tab4);
			class MyThread implements Runnable {
				String imgPath;
				public MyThread (String imgPath) {
					this.imgPath = imgPath;
				}
				public void run (){
					Bitmap bitmap = decodeSampledBitmapFromPath(imgPath);
					imgAdapt.addPic(bitmap, currentPic);
					invalidator();
				}
			}
			Runnable r = new MyThread(imgPath);
			new Thread(r).start();
			vg.invalidate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

		/* *
		 * Invalidates the current view, so it will get redrawn (not working).
		 * 
		 */
	public void invalidator(){
//		picView = (ImageView) findViewById(R.id.tab4_picture);
//  		picGallery = (Gallery) findViewById(R.id.tab4_gallery);
//  		picView.invalidate();
//  		picGallery.invalidate();
//		ViewGroup vg = (ViewGroup) findViewById(R.id.tab4);
//		vg.invalidate();
	}
	

	/* *
	 * Decodes a bitmap from a location
	 * 
	 * @param imgPath	The location of the picture that is to be decoded.
	 * @return BitmapFactory.decodeFile(imgPath, bmpOptions)	Returns a decoded bitmap from imgPath.
	 * @see 
	 */
	public static Bitmap decodeSampledBitmapFromPath(String imgPath) {
		//set the width and height we want to use as maximum display
		int targetWidth = 600;
		int targetHeight = 400;

		//sample the incoming image to save on memory resources

		//create bitmap options to calculate and use sample size
		BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

		//first decode image dimensions only - not the image bitmap itself
		bmpOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgPath, bmpOptions);

		//work out what the sample size should be

		//image width and height before sampling
		int currHeight = bmpOptions.outHeight;
		int currWidth = bmpOptions.outWidth;

		//variable to store new sample size
		int sampleSize = 1;

		//calculate the sample size if the existing size is larger than target size
		if (currHeight>targetHeight || currWidth>targetWidth) 
		{
			//use either width or height
			if (currWidth>currHeight)
				sampleSize = Math.round((float)currHeight/(float)targetHeight);
			else 
				sampleSize = Math.round((float)currWidth/(float)targetWidth);
		}
		//use the new sample size
		bmpOptions.inSampleSize = sampleSize;

		//now decode the bitmap using sample options
		bmpOptions.inJustDecodeBounds = false;
		
		//get the file as a bitmap
		return BitmapFactory.decodeFile(imgPath, bmpOptions);
	}

	public static boolean isResetPressed() {
		return resetPressed;
	}

	public static void setResetPressed(boolean resetPressed) {
		MainActivity.resetPressed = resetPressed;
	}
}
