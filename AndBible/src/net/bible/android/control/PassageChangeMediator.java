package net.bible.android.control;

import net.bible.android.control.event.passage.BeforeCurrentPageChangeEvent;
import net.bible.android.control.event.passage.CurrentVerseChangedEvent;
import net.bible.android.control.page.splitscreen.SplitScreenControl;
import net.bible.android.view.activity.page.MainBibleActivity;
import net.bible.service.device.ScreenSettings;
import android.util.Log;
import de.greenrobot.event.EventBus;

/** when a bible passage is changed there are lots o things to update and they should be done in a helpful order
 * This helps to control screen updates after a passage change
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class PassageChangeMediator {

	private MainBibleActivity mMainBibleActivity;
	private BibleContentManager mBibleContentManager;
	private SplitScreenControl mSplitScreenControl = ControlFactory.getInstance().getSplitScreenControl();
	private boolean isPageChanging = false;

	private static final String TAG = "PassageChangeMediator";
	
	private static final PassageChangeMediator singleton = new PassageChangeMediator();
	
	public static final PassageChangeMediator getInstance() {
		return singleton;
	}

	/** first time we know a page or doc will imminently change
	 */
	public void onBeforeCurrentPageChanged() {
		isPageChanging = true;

		EventBus.getDefault().post(new BeforeCurrentPageChangeEvent());
	}
	
	/** the document has changed so ask the view to refresh itself
	 */
	public void onCurrentPageChanged() {
		if (mBibleContentManager!=null) {
			mBibleContentManager.updateText();
		} else {
			Log.w(TAG, "BibleContentManager not yet registered");
		}
	}

	/** the document has changed so ask the view to refresh itself
	 */
	public void forcePageUpdate() {
		if (mBibleContentManager!=null) {
			mBibleContentManager.updateText(true);
		} else {
			Log.w(TAG, "BibleContentManager not yet registered");
		}
	}

	/** this is triggered on scroll
	 */
	public void onCurrentVerseChanged() {
		EventBus.getDefault().post(new CurrentVerseChangedEvent());
		
		mSplitScreenControl.synchronizeScreens();
	}

	/** The thread which fetches the new page html has started
	 */
	public void contentChangeStarted() {
		isPageChanging = true;

		// only update occasionally otherwise black-on-black or w-on-w may occur in variable light conditions
		ScreenSettings.isNightModeChanged();

		if (mMainBibleActivity!=null) {
			mMainBibleActivity.onPassageChangeStarted();
		} else {
			Log.w(TAG, "Bible activity not yet registered");
		}
	}
	/** finished fetching html so should hide hourglass
	 */
	public void contentChangeFinished() {
		if (mMainBibleActivity!=null) {
			mMainBibleActivity.onPassageChanged();
			mSplitScreenControl.synchronizeScreens();
		} else {
			Log.w(TAG, "Bible activity not yet registered");
		}

		isPageChanging = false;
	}
	
	public boolean isPageChanging() {
		return isPageChanging;
	}

	public void setBibleContentManager(BibleContentManager bibleContentManager) {
		this.mBibleContentManager = bibleContentManager;
	}

	public void setMainBibleActivity(MainBibleActivity mainBibleActivity) {
		Log.i(TAG, "setMainBibleActivity in PassageChangeMediator.  Previous mainBibleActivity="+mMainBibleActivity);

		this.mMainBibleActivity = mainBibleActivity;
	}
}
