package cn.edu.csu.iteliter;

import java.util.ArrayList;
import java.util.List;

import weibo4j.Friendships;
import weibo4j.Timeline;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.User;
import weibo4j.model.WeiboException;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.edu.csu.iteliter.customview.TimelineAdapter;
import cn.edu.csu.iteliter.customview.TimelineListView;
import cn.edu.csu.iteliter.customview.TimelineListView.OnRefreshListener;
import cn.edu.csu.iteliter.model.UserData;
import cn.edu.csu.iteliter.model.WeiboImage;
import cn.edu.csu.iteliter.util.CacheUtil;
import cn.edu.csu.iteliter.util.ConstantUtil;
import cn.edu.csu.iteliter.util.ToastUtil;
import cn.edu.csu.iteliter.util.UserDataUtil;
import cn.edu.csu.iteliter.util.WeiboUtil;

/**
 * MainWeibo activity
 * 
 * @author hjw
 * 
 */
public class MainWeibo extends Activity implements ConstantUtil {

	/**
	 * click on tab
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mTabPager.setCurrentItem(index);
		}
	}

	/**
	 * pager change listener
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_pressed));
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, 0, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
				}
				break;
			case 1:
				mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_pressed));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, one, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
				}
				break;
			case 2:
				mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_pressed));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, two, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(three, two, 0, 0);
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
				}
				break;
			case 3:
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, three, 0, 0);
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, three, 0, 0);
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, three, 0, 0);
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
				}
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);
			animation.setDuration(150);
			mTabImg.startAnimation(animation);
		}
	}

	public static Handler imageHandler;// used to async load image
	public static MainWeibo instance = null;// always helpful
	public static User user;// the current login user
	public static UserData userData;// share with other activities

	private int baseAPP = 0;
	private final int BLOG_URL_MAX_LENGTH = 10;
	private LinearLayout btn_menu_close;
	private CheckBox cb_settings_sound;//
	private int count = 20;// default count
	private int currentType = TIMELINE_FRIENDS;// default type --> current timeline type
	private int currIndex = 0;//
	private int feature = 0;
	public LayoutInflater inflater;// used to load layout
	private boolean isFollowd = false;
	private ImageView iv_home_head;
	private ImageView iv_info_gender;// 性别
	// /////////////////////// tab info //////////////////////////////
	private ImageView iv_info_photo;// 头像

	private List<Status> listFriendsTimeline;
	private List<Status> listMentionsTimeline;
	private List<Status> listPublicTimeline;
	private List<Status> listUserTimeline;
	private LinearLayout ll_menu_close;

	private TimelineListView lvFriendsTimeline;

	private TimelineListView lvMentionTimeline;;

	private TimelineListView lvPublicTimeline;

	private TimelineListView lvUserTimeline;

	private View mainMenu;

	private boolean menu_display = false;

	private PopupWindow menuWindow;
	private ImageView mTab1, mTab2, mTab3, mTab4;
	private ImageView mTabImg;//
	private ViewPager mTabPager;

	public MediaPlayer musicPlayer;
	private int one;//
	// private RadioGroup rg_shake_mode;//
	private RadioGroup rg_shake_feature;
	private RelativeLayout rl_settings_followauthor;

	// ///////////////// tab Home ////////////////////////
	private View rlHomeLoading;
	private RelativeLayout rlInfoLoading;
	private long sinceIdFriends = 2L;
	private long sinceIdMentions = 2L;

	private long sinceIdPublic = 2L;// default is 2,must > 1
	private long sinceIdUser = 2L;
	private int sound_refresh = R.raw.sinaweibo_refresh;
	private double spaceUsed;

	private ScrollView sv_userinfo_info;
	private View tabHome;// four tabs
	private Handler tabhomehandler;// handler timeline loading finished

	private Handler tabinfohandler;
	private View tabSettings;

	private View tabShake;
	private View tabUserInfo;

	private int three;

	private TimelineAdapter timelineAdapterFriends;

	private TimelineAdapter timelineAdapterMentions;

	private TimelineAdapter timelineAdapterPublic;

	private TimelineAdapter timelineAdapterUser;

	private TextView tv_home_name;

	private TextView tv_info_blogurl;// 博客地址

	private TextView tv_info_description;// 个人简介

	private TextView tv_info_favourites_count;// 收藏数

	private TextView tv_info_followers_count;// 粉丝数
	private TextView tv_info_friends_count;// 关注数
	private TextView tv_info_location;// 所在地
	private TextView tv_info_online_status;// 在线状态
	private TextView tv_info_screenname;// 昵称
	private TextView tv_info_statuses_count;// 微博数
	private TextView tv_settings_followauthor;
	private TextView tv_settings_spaceused;
	private int two;
	private int zero = 0;//

	// asyncload space used
	private void asyncLoadSpaceUsed() {
		new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
			protected Void doInBackground(Void... params) {
				spaceUsed = CacheUtil.calculateSpaceUsed();
				System.out.println(spaceUsed);
				return null;
			}

			protected void onPostExecute(Void result) {
				tv_settings_spaceused.setText("已用空间 " + WeiboUtil.formatSpaceSize(spaceUsed));
			}
		}.execute();
	}

	// tab home 显示功能列表dialog
	public void btn_home_showdialog(View v) {
		Intent intent = new Intent(MainWeibo.this, HomeTopRightDialog.class);
		startActivityForResult(intent, REQUEST_WEIBO_TYPE);
	}

	public void btn_home_writeweibo(View v) {
		Intent intent = new Intent(MainWeibo.this, HomeWeiboWrite.class);
		intent.putExtra(WRITE_WEIBO_TYPE, WRITE_WEIBO_TYPE_WRITEWEIBO);
		startActivity(intent);
	}

	// exit from settings
	public void btn_settings_exit(View v) {
		Intent intent = new Intent(MainWeibo.this, DialogSettingsExit.class);
		startActivity(intent);
	}

	// really do load timeline part // make sure no thread start in it!
	private void doLoadTimeline(final int type) {
		System.out.println("load timeline running...");
		Timeline timeline = new Timeline();
		timeline.client.setToken(userData.getToken());
		StatusWapper statusWapper = null;
		Paging paging = new Paging();
		try {
			switch (type) {
			case TIMELINE_PUBLIC:
				paging.setSinceId(sinceIdPublic);
				statusWapper = timeline.getPublicTimeline(count, baseAPP);
				sinceIdPublic = Long.parseLong(statusWapper.getStatuses().get(0).getId());
				insertTimelineBefore(listPublicTimeline, statusWapper.getStatuses());
				break;
			case TIMELINE_FRIENDS:
				paging.setSinceId(sinceIdFriends);
				statusWapper = timeline.getFriendsTimeline(baseAPP, feature, paging);
				if (statusWapper.getStatuses().size() > 0) {
					sinceIdFriends = Long.parseLong(statusWapper.getStatuses().get(0).getId());
				}
				insertTimelineBefore(listFriendsTimeline, statusWapper.getStatuses());
				break;
			case TIMELINE_USER:
				paging.setSinceId(sinceIdUser);
				statusWapper = timeline.getUserTimelineByUid(userData.getUserid(), paging, baseAPP, feature);// userid
				if (statusWapper.getStatuses().size() > 0) {
					sinceIdUser = Long.parseLong(statusWapper.getStatuses().get(0).getId());
				}
				insertTimelineBefore(listUserTimeline, statusWapper.getStatuses());
				break;
			case TIMELINE_MENTIONS:
				paging.setSinceId(sinceIdMentions);
				statusWapper = timeline.getMentions(paging, 0, 0, 0);// mentions three filters
				if (statusWapper.getStatuses().size() > 0) {
					sinceIdMentions = Long.parseLong(statusWapper.getStatuses().get(0).getId());
				}
				insertTimelineBefore(listMentionsTimeline, statusWapper.getStatuses());
				break;
			default:
				paging.setSinceId(sinceIdPublic);
				statusWapper = timeline.getPublicTimeline(count, baseAPP);
				if (statusWapper.getStatuses().size() > 0) {
					sinceIdPublic = Long.parseLong(statusWapper.getStatuses().get(0).getId());
				}
				insertTimelineBefore(listPublicTimeline, statusWapper.getStatuses());
				break;
			}
		} catch (WeiboException e) {
			e.printStackTrace();// TODO:deal with the exception
		}
		if (statusWapper == null) {
			System.out.println("status wrapper is null");// TODO: deal with no result
			return;
		}
		System.out.println("load timeline ok");
	}

	// follow
	private void followAuthor() {
		ToastUtil.showShortToast(getApplicationContext(), "正在关注作者微博，稍等哈...");
		new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
			protected Void doInBackground(Void... params) {
				Friendships fm = new Friendships();
				fm.client.setToken(userData.getToken());
				try {
					fm.createFriendshipsById(ConstantUtil.AUTHOR_UID);
				} catch (WeiboException e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				isFollowd = true;
				tv_settings_followauthor.setText("已经关注了");
				ToastUtil.showShortToast(getApplicationContext(), "成功关注作者微博！");
				userData.setFollowauthor(isFollowd);
				UserDataUtil.updateLocalToken(getApplicationContext(), userData);
			}
		}.execute();
	}

	// hide all the list view in tab home
	private void hideAllListVIew() {
		lvPublicTimeline.setVisibility(View.GONE);
		lvUserTimeline.setVisibility(View.GONE);
		lvMentionTimeline.setVisibility(View.GONE);
		lvFriendsTimeline.setVisibility(View.GONE);
		rlHomeLoading.setVisibility(View.VISIBLE);
	}

	// ///////////////////////////// tab shake //////////////////////////////////

	// init tab home ui , and init timeline handler
	private void initTabHomeUI() {
		lvPublicTimeline = (TimelineListView) tabHome.findViewById(R.id.lvPublicTimeline);
		lvUserTimeline = (TimelineListView) tabHome.findViewById(R.id.lvUserTimeline);
		lvMentionTimeline = (TimelineListView) tabHome.findViewById(R.id.lvMentionTimeline);
		lvFriendsTimeline = (TimelineListView) tabHome.findViewById(R.id.lvFriendsTimeline);
		rlHomeLoading = tabHome.findViewById(R.id.rlHomeLoading);
		iv_home_head = (ImageView) tabHome.findViewById(R.id.iv_home_head);
		tv_home_name = (TextView) tabHome.findViewById(R.id.tv_home_name);

		listFriendsTimeline = new ArrayList<Status>();
		listPublicTimeline = new ArrayList<Status>();
		listUserTimeline = new ArrayList<Status>();
		listMentionsTimeline = new ArrayList<Status>();

		lvFriendsTimeline.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {// AsyncTask:call in UI thread
					protected Void doInBackground(Void... params) {
						doLoadTimeline(currentType);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if (userData.isSoundPlay()) {
							musicPlayer.start();
						}
						timelineAdapterFriends.notifyDataSetChanged();
						lvFriendsTimeline.onRefreshComplete();
					}
				}.execute();
			}
		});

		lvPublicTimeline.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
					protected Void doInBackground(Void... params) {
						doLoadTimeline(currentType);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if (userData.isSoundPlay()) {
							musicPlayer.start();
						}
						timelineAdapterPublic.notifyDataSetChanged();
						lvPublicTimeline.onRefreshComplete();
					}
				}.execute();
			}
		});

		lvUserTimeline.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
					protected Void doInBackground(Void... params) {
						doLoadTimeline(currentType);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if (userData.isSoundPlay()) {
							musicPlayer.start();
						}
						timelineAdapterUser.notifyDataSetChanged();
						lvUserTimeline.onRefreshComplete();
					}
				}.execute();
			}
		});

		lvMentionTimeline.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
					protected Void doInBackground(Void... params) {
						doLoadTimeline(currentType);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if (userData.isSoundPlay()) {
							musicPlayer.start();
						}
						timelineAdapterMentions.notifyDataSetChanged();
						lvMentionTimeline.onRefreshComplete();
					}
				}.execute();
			}
		});

		tabhomehandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				System.out.println("handle message");
				if (message.what == MESSAGE_TYPE_TIMELINE) {// after get timeline
					System.out.println("message handler --- timeline");
					refreshTimeline(message);
				} else if (message.what == MESSAGE_TYPE_USERDATA) {
					// after get userdata ---> first time: user nicnk name and profile image should load
					System.out.println("message handler --- userdata");
					user = (User) message.obj;//
					tv_home_name.setText(user.getScreenName());
					userData.setNickname(user.getScreenName());
					userData.setProfileimage(user.getProfileImageUrl());
					UserDataUtil.updateLocalToken(getApplicationContext(), userData);// update user info --->save
					WeiboUtil.restoreBitmap(CacheUtil.PROFILE_CACHE_PATH, user.getProfileImageUrl(), imageHandler,
							iv_home_head, IMAGE_TYPE_PROFILE);
				}
				// if (message.what == MESSAGE_TYPE_WEIBOIMAGE) {
				// System.out.println("message handler --- weibo image");
				// WeiboImage weiboImage = (WeiboImage) message.obj;
				// weiboImage.imageView.setImageBitmap(weiboImage.bitmap);
				// }
			}
		};

		if ((userData.getNickname() == null) || userData.getNickname().equalsIgnoreCase("")
				|| (userData.getProfileimage() == null) || userData.getProfileimage().equalsIgnoreCase("")) {
			System.out.println("----------load userdata-------------");
			WeiboUtil.asyncLoadUserData(tabhomehandler);//
		} else {// do not validate user profile image, for user may change his profile image
			System.out.println("----------no need to load userdata-------------");
			tv_home_name.setText(userData.getNickname());// nick name
			WeiboUtil.restoreBitmap(CacheUtil.PROFILE_CACHE_PATH, userData.getProfileimage(), imageHandler,
					iv_home_head, IMAGE_TYPE_PROFILE);
		}

	}

	public void initTabSettingsUI() {
		cb_settings_sound = (CheckBox) tabSettings.findViewById(R.id.cb_settings_sound);
		if (userData.isSoundPlay()) {
			cb_settings_sound.setChecked(true);
		} else {
			cb_settings_sound.setChecked(false);
		}
		// validate whether the user has followed the author
		rl_settings_followauthor = (RelativeLayout) tabSettings.findViewById(R.id.rl_settings_followauthor);
		tv_settings_followauthor = (TextView) tabSettings.findViewById(R.id.tv_settings_followauthor);
		tv_settings_spaceused = (TextView) tabSettings.findViewById(R.id.tv_settings_spaceused);
		rl_settings_followauthor.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isFollowd) {// follow
					followAuthor();
				} else {// not follow
					unfollowAuthor();
				}
			}
		});
		cb_settings_sound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				userData.setSoundPlay(isChecked);
				UserDataUtil.updateLocalToken(getApplicationContext(), userData);
			}
		});
		if (userData.isFollowauthor()) {
			tv_settings_followauthor.setText("已经关注了");
		} else {
			tv_settings_followauthor.setText("还未关注哟");
		}
		tv_settings_spaceused.setText("");
		asyncLoadSpaceUsed();
	}

	private void initTabShakeUI() {
		// rg_shake_mode = (RadioGroup) tabShake.findViewById(R.id.rg_shake_mode);//
		rg_shake_feature = (RadioGroup) tabShake.findViewById(R.id.rg_shake_feature);
	}

	// // mode weibo
	// public void shake_modeweibo(View v) {
	// int type = WEIBO_TYPE_HAPPY;
	// if (rg_shake_mode.getCheckedRadioButtonId() == R.id.rb_mode_sad) {
	// type = WEIBO_TYPE_SAD;
	// } else if (rg_shake_mode.getCheckedRadioButtonId() == R.id.rb_mode_wuliao) {
	// type = WEIBO_TYPE_WULIAO;
	// }
	// Intent intent = new Intent(MainWeibo.this, ShakeWeibo.class);
	// intent.putExtra(SHAKE_TYPE, SHAKE_TYPE_MODEWEIBO);
	// intent.putExtra(WEIBO_TYPE, type);
	// startActivity(intent);
	// }

	// init user info ui
	public void initTabUserInfoUI() {
		iv_info_photo = (ImageView) tabUserInfo.findViewById(R.id.iv_info_photo);
		tv_info_screenname = (TextView) tabUserInfo.findViewById(R.id.tv_info_screenname);
		iv_info_gender = (ImageView) tabUserInfo.findViewById(R.id.iv_info_gender);
		tv_info_location = (TextView) tabUserInfo.findViewById(R.id.tv_info_location);
		tv_info_online_status = (TextView) tabUserInfo.findViewById(R.id.tv_info_online_status);
		tv_info_blogurl = (TextView) tabUserInfo.findViewById(R.id.tv_info_blogurl);
		tv_info_description = (TextView) tabUserInfo.findViewById(R.id.tv_info_description);
		tv_info_followers_count = (TextView) tabUserInfo.findViewById(R.id.tv_info_followers_count);
		tv_info_friends_count = (TextView) tabUserInfo.findViewById(R.id.tv_info_friends_count);
		tv_info_statuses_count = (TextView) tabUserInfo.findViewById(R.id.tv_info_statuses_count);
		tv_info_favourites_count = (TextView) tabUserInfo.findViewById(R.id.tv_info_favourites_count);
		rlInfoLoading = (RelativeLayout) tabUserInfo.findViewById(R.id.rlInfoLoading);
		sv_userinfo_info = (ScrollView) tabUserInfo.findViewById(R.id.sv_userinfo_info);

		tabinfohandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				System.out.println("handle message");
				if (message.what == MESSAGE_TYPE_USERDATA) {
					System.out.println("message handler --- userdata");
					user = (User) message.obj;//
					setUserInfo();
				}
			}
		};

		if (user == null) {
			rlInfoLoading.setVisibility(View.VISIBLE);
			sv_userinfo_info.setVisibility(View.GONE);
			WeiboUtil.asyncLoadUserData(tabinfohandler);
		} else {
			rlInfoLoading.setVisibility(View.GONE);
			sv_userinfo_info.setVisibility(View.VISIBLE);
			setUserInfo();
		}

	}

	// insert to before
	private void insertTimelineBefore(List<Status> listTimeline, List<Status> statuses) {
		listTimeline.addAll(0, statuses);
	}

	// ///////////////////////////////// tab settings ////////////////////////////////////////////////////

	// load timeline according to the type
	// if flag is true, force to refresh---> change
	// if flag is true,means show loading view,otherwise just show listview header
	private void loadTimeline(final int type) {
		System.out.println("load timeline: type = " + type);
		// if (!refreshCurrent) {// not refresh --> then load timeline
		// first check timeline already load?
		ListAdapter listAdapter = null;
		if (type == TIMELINE_PUBLIC) {
			listAdapter = lvPublicTimeline.getAdapter();
		} else if (type == TIMELINE_FRIENDS) {
			listAdapter = lvFriendsTimeline.getAdapter();
		} else if (type == TIMELINE_USER) {
			listAdapter = lvUserTimeline.getAdapter();
		} else if (type == TIMELINE_MENTIONS) {
			listAdapter = lvMentionTimeline.getAdapter();
		}
		if (listAdapter != null) {// change timeline
			showTimeline(type);// direct show the timeline
			return;
		}
		// not load,start first load
		hideAllListVIew();
		// }
		new Thread(new Runnable() {
			public void run() {
				doLoadTimeline(type);
				// if (!refreshCurrent) {// if not refresh then send message
				Message message = new Message();
				message.what = MESSAGE_TYPE_TIMELINE;
				message.arg1 = type;
				tabhomehandler.sendMessage(message);
				// }
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_WEIBO_TYPE) {// request for which weibo type
			if (resultCode == RESULT_OK) {// result is ok
				// ToastUtil.showShortToast(getApplicationContext(), data.getAction());//no action --> type
				loadTimeline(data.getIntExtra(TIMELINE_TYPE, TIMELINE_PUBLIC));
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_weibo);
		instance = this;
		userData = UserDataUtil.readUserData(getApplicationContext());
		musicPlayer = MediaPlayer.create(getApplicationContext(), sound_refresh);
		musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		// tabs
		mTab1 = (ImageView) findViewById(R.id.img_weixin);
		mTab2 = (ImageView) findViewById(R.id.img_address);
		mTab3 = (ImageView) findViewById(R.id.img_friends);
		mTab4 = (ImageView) findViewById(R.id.img_settings);
		mTabImg = (ImageView) findViewById(R.id.img_tab_now);
		mTab1.setOnClickListener(new MyOnClickListener(0));
		mTab2.setOnClickListener(new MyOnClickListener(1));
		mTab3.setOnClickListener(new MyOnClickListener(2));
		mTab4.setOnClickListener(new MyOnClickListener(3));
		int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		one = displayWidth / 4; //
		two = one * 2;
		three = one * 3;
		// views
		// LayoutInflater mlayoutInflater = LayoutInflater.from(this);// layout inflater//
		tabHome = inflater.inflate(R.layout.main_tab_home, null);
		tabUserInfo = inflater.inflate(R.layout.main_tab_userinfo, null);
		tabShake = inflater.inflate(R.layout.main_tab_shake, null);
		tabSettings = inflater.inflate(R.layout.main_tab_settings, null);
		final ArrayList<View> views = new ArrayList<View>();
		views.add(tabHome);
		views.add(tabUserInfo);
		views.add(tabShake);
		views.add(tabSettings);
		// tabpager
		mTabPager = (ViewPager) findViewById(R.id.tabpager_mainweibo);
		mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());
		PagerAdapter mPagerAdapter = new PagerAdapter() {
			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
		};

		mTabPager.setAdapter(mPagerAdapter);

		imageHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.what == MESSAGE_TYPE_WEIBOIMAGE) {
					System.out.println("imageHandler --- message handler --- weibo image");
					WeiboImage weiboImage = (WeiboImage) message.obj;
					weiboImage.imageView.setImageBitmap(weiboImage.bitmap);
				}
			}
		};

		initTabHomeUI();// just init tabhome ui
		initTabUserInfoUI();
		initTabShakeUI();
		initTabSettingsUI();
		loadTimeline(TIMELINE_FRIENDS);// start to load weibo here
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (musicPlayer.isPlaying()) {
			musicPlayer.stop();
		}
		musicPlayer.release();
	}

	// on key down
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
			if (menu_display) {
				menuWindow.dismiss();
				menu_display = false;
			} else {
				Intent intent = new Intent();
				intent.setClass(MainWeibo.this, DialogExit.class);
				startActivity(intent);
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (!menu_display) {
				mainMenu = inflater.inflate(R.layout.main_menu, null);
				menuWindow = new PopupWindow(mainMenu, android.view.ViewGroup.LayoutParams.FILL_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				// menuWindow.showAsDropDown(layout);
				// menuWindow.showAsDropDown(null, 0, layout.getHeight());
				menuWindow.showAtLocation(findViewById(R.id.rl_mainweibo), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
						0, 0);
				ll_menu_close = (LinearLayout) mainMenu.findViewById(R.id.ll_menu_close);
				btn_menu_close = (LinearLayout) mainMenu.findViewById(R.id.btn_menu_close);

				btn_menu_close.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.setClass(MainWeibo.this, DialogExit.class);
						startActivity(intent);
						menuWindow.dismiss();
					}
				});
				menu_display = true;
			} else {
				menuWindow.dismiss();
				menu_display = false;
			}
			return false;
		}
		return false;
	}

	// refresh timeline according to the message
	// message.obj -> listitems what -> type
	private void refreshTimeline(Message msg) {
		System.out.println("refresh timeline: type =  " + msg.arg1);
		switch (msg.arg1) {
		case TIMELINE_PUBLIC:
			timelineAdapterPublic = new TimelineAdapter(listPublicTimeline);
			lvPublicTimeline.setAdapter(timelineAdapterPublic);
			break;
		case TIMELINE_FRIENDS:
			timelineAdapterFriends = new TimelineAdapter(listFriendsTimeline);
			lvFriendsTimeline.setAdapter(timelineAdapterFriends);
			break;
		case TIMELINE_USER:
			timelineAdapterUser = new TimelineAdapter(listUserTimeline);
			lvUserTimeline.setAdapter(timelineAdapterUser);
			break;
		case TIMELINE_MENTIONS:
			timelineAdapterMentions = new TimelineAdapter(listMentionsTimeline);
			lvMentionTimeline.setAdapter(timelineAdapterMentions);
			break;
		default:
			timelineAdapterPublic = new TimelineAdapter(listPublicTimeline);
			lvPublicTimeline.setAdapter(timelineAdapterPublic);
			break;
		}
		showTimeline(msg.arg1);// show
	}

	// enter about us
	public void rl_settings_aboutus(View v) {
		Intent intent = new Intent(MainWeibo.this, SettingsAboutUs.class);
		startActivity(intent);
	}

	// clear cache
	public void rl_settings_clearcache(View v) {
		ToastUtil.showShortToast(getApplicationContext(), "正在清空缓存图片，稍等哈...");
		new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
			protected Void doInBackground(Void... params) {
				CacheUtil.clearCache();
				return null;
			}

			protected void onPostExecute(Void result) {
				ToastUtil.showShortToast(getApplicationContext(), "缓存图片已清空！");
			}
		}.execute();
	}

	// suggestion from settings
	public void rl_settings_suggestion(View v) {
		Intent intent = new Intent(MainWeibo.this, HomeWeiboWrite.class);
		intent.putExtra(WRITE_WEIBO_TYPE, WRITE_WEIBO_TYPE_SUGGESTION);
		startActivity(intent);
	}

	// set user info to controls
	private void setUserInfo() {
		rlInfoLoading.setVisibility(View.GONE);
		sv_userinfo_info.setVisibility(View.VISIBLE);
		tv_info_screenname.setText(user.getScreenName());
		tv_info_location.setText(user.getLocation());
		if (user.getOnlineStatus() == 0) {
			tv_info_online_status.setText("不在线");
		} else if (user.getOnlineStatus() == 1) {
			tv_info_online_status.setText("在线");
		}

		if ((user.getUrl() == null) || "".equalsIgnoreCase(user.getUrl())) {
			tv_info_blogurl.setText("暂无博客");
		} else {
			if (user.getUrl().length() > BLOG_URL_MAX_LENGTH) {
				tv_info_blogurl.setText(user.getUrl().substring(0, BLOG_URL_MAX_LENGTH) + "...");
			} else {
				tv_info_blogurl.setText(user.getUrl());
			}
		}

		if (user.getDescription() == null) {
			tv_info_description.setText("暂无简介");
		} else {
			tv_info_description.setText(user.getDescription());
		}

		tv_info_followers_count.setText(user.getFollowersCount() + "");
		tv_info_friends_count.setText(user.getFriendsCount() + "");
		tv_info_statuses_count.setText(user.getStatusesCount() + "");
		tv_info_favourites_count.setText(user.getFavouritesCount() + "");
		if (user.getGender().equalsIgnoreCase("f")) {// female
			iv_info_gender.setImageResource(R.drawable.userinfo_female);
		} else if (user.getGender().equalsIgnoreCase("m")) {
			iv_info_gender.setImageResource(R.drawable.userinfo_male);
		} else {// not know
			iv_info_gender.setVisibility(View.GONE);
		}
		WeiboUtil.restoreBitmap(CacheUtil.PROFILE_CACHE_PATH, user.getProfileImageUrl(), tabinfohandler, iv_info_photo,
				IMAGE_TYPE_PROFILE);
	}

	// add friend
	public void shake_addfriend(View v) {
		Intent intent = new Intent(MainWeibo.this, ShakeWeibo.class);
		intent.putExtra(SHAKE_TYPE, SHAKE_TYPE_ADDFRIEND);
		startActivity(intent);
	}

	// feature weibo
	public void shake_featureweibo(View v) {
		int type = WEIBO_TYPE_PICTURE;
		if (rg_shake_feature.getCheckedRadioButtonId() == R.id.rb_feature_music) {
			type = WEIBO_TYPE_MUSIC;
		} else if (rg_shake_feature.getCheckedRadioButtonId() == R.id.rb_feature_video) {
			type = WEIBO_TYPE_VIDEO;
		}
		Intent intent = new Intent(MainWeibo.this, ShakeWeibo.class);
		intent.putExtra(SHAKE_TYPE, SHAKE_TYPE_FEATUREWEIBO);
		intent.putExtra(WEIBO_TYPE, type);
		startActivity(intent);
	}

	// nearby weibo
	public void shake_nearbyweibo(View v) {
		Intent intent = new Intent(MainWeibo.this, ShakeWeibo.class);
		intent.putExtra(SHAKE_TYPE, SHAKE_TYPE_NEARBYWEIBO);
		startActivity(intent);
	}

	// show timeline according to the type
	private void showTimeline(int type) {
		if (userData.isSoundPlay()) {
			musicPlayer.start();
		}
		currentType = type;
		System.out.println("show timeline: type =  " + type);
		hideAllListVIew();
		rlHomeLoading.setVisibility(View.GONE);//
		switch (type) {
		case TIMELINE_PUBLIC:
			lvPublicTimeline.setVisibility(View.VISIBLE);
			break;
		case TIMELINE_FRIENDS:
			lvFriendsTimeline.setVisibility(View.VISIBLE);
			break;
		case TIMELINE_USER:
			lvUserTimeline.setVisibility(View.VISIBLE);
			break;
		case TIMELINE_MENTIONS:
			lvMentionTimeline.setVisibility(View.VISIBLE);
			break;
		default:
			lvPublicTimeline.setVisibility(View.VISIBLE);
			break;
		}
	}

	// unfollow
	private void unfollowAuthor() {
		ToastUtil.showShortToast(getApplicationContext(), "正在取消关注作者微博，稍等哈...");
		new AsyncTask<Void, Void, Void>() {// AsyncTask:call from UI thread
			protected Void doInBackground(Void... params) {
				Friendships fm = new Friendships();
				fm.client.setToken(userData.getToken());
				try {
					fm.destroyFriendshipsDestroyById(ConstantUtil.AUTHOR_UID);
				} catch (WeiboException e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				isFollowd = false;
				tv_settings_followauthor.setText("还未关注哟");
				ToastUtil.showShortToast(getApplicationContext(), "取消关注作者微博！");
				userData.setFollowauthor(isFollowd);
				UserDataUtil.updateLocalToken(getApplicationContext(), userData);
			}
		}.execute();
	}

}
