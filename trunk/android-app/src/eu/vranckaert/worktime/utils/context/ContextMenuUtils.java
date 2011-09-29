package eu.vranckaert.worktime.utils.context;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.TimeRegistrationsActivity;
import eu.vranckaert.worktime.activities.timeregistrations.*;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;

/**
 * User: DIRK VRANCKAERT
 * Date: 14/05/11
 * Time: 16:41
 */
public class ContextMenuUtils {
    private static final String LOG_TAG = ContextMenuUtils.class.getSimpleName();

    /**
     * Create the context menu for an edit action on a {@link TimeRegistration}.
     * @param ctx The context.
     * @param registrationForContext The {@link TimeRegistration}.
     * @param menu The menu to add options to.
     * @param inDetailView {@link Boolean#TRUE} if called from within the details view. Else {@link Boolean#FALSE}.
     */
    public static final void createTimeRegistrationEditContextMenu(Context ctx, TimeRegistration registrationForContext, ContextMenu menu, boolean inDetailView) {
        Log.d(LOG_TAG, "Creating context menu...");

        menu.setHeaderTitle(ctx.getString(R.string.lbl_registrations_menu_title));

        if (!inDetailView) {
            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TIME_REGISTRATION_DETAILS,
                    Menu.NONE,
                    R.string.lbl_registrations_menu_details
            );
        }
        menu.add(Menu.NONE,
                Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_START,
                Menu.NONE,
                R.string.lbl_registrations_menu_edit_start
        );
        if (!registrationForContext.isOngoingTimeRegistration()) {
            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_END,
                    Menu.NONE,
                    R.string.lbl_registrations_menu_edit_end
            );
            if (StringUtils.isNotBlank(registrationForContext.getComment())) {
                menu.add(Menu.NONE,
                        Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_COMMENT,
                        Menu.NONE,
                        R.string.lbl_registrations_menu_edit_comment
                );
            } else {
                menu.add(Menu.NONE,
                        Constants.ContentMenuItemIds.TIME_REGISTRATION_ADD_COMMENT,
                        Menu.NONE,
                        R.string.lbl_registrations_menu_add_comment
                );
            }
        }
        menu.add(Menu.NONE,
                Constants.ContentMenuItemIds.TIME_REGISTRATION_RESTART,
                Menu.NONE,
                R.string.lbl_registration_menu_restart
        );
        menu.add(Menu.NONE,
                Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_PROJECT_TASK,
                Menu.NONE,
                R.string.lbl_registrations_menu_edit_project_task
        );
        if (!inDetailView) {
            menu.add(Menu.NONE,
                Constants.ContentMenuItemIds.TIME_REGISTRATION_DELETE,
                Menu.NONE,
                R.string.lbl_registrations_menu_delete
            );
        }
    }

    public static final boolean handleTimeRegistrationEditContextMenuSelection(Activity activity, MenuItem item,
                                          TimeRegistration timeRegistrationForContext,
                                          TimeRegistration previousTimeRegistration,
                                          TimeRegistration nextTimeRegistration,
                                          AnalyticsTracker tracker) {
        String trackerEvent = null;

        switch (item.getItemId()) {
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_DETAILS: {
                IntentUtil.openRegistrationDetailActivity(activity, timeRegistrationForContext,
                        previousTimeRegistration, nextTimeRegistration);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_START: {
                trackerEvent = TrackerConstants.EventActions.EDIT_TR_START_TIME;
                Intent intent = new Intent(activity, EditTimeRegistrationStartTimeActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistrationForContext);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_PREVIOUS, previousTimeRegistration);
                activity.startActivityForResult(intent, Constants.IntentRequestCodes.REGISTRATION_EDIT_DIALOG);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_END: {
                trackerEvent = TrackerConstants.EventActions.EDIT_TR_END_TIME;
                Intent intent = new Intent(activity, EditTimeRegistrationEndTimeActivity.class);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION, timeRegistrationForContext);
                intent.putExtra(Constants.Extras.TIME_REGISTRATION_NEXT, nextTimeRegistration);
                activity.startActivityForResult(intent, Constants.IntentRequestCodes.REGISTRATION_EDIT_DIALOG);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_ADD_COMMENT: {
                trackerEvent = TrackerConstants.EventActions.ADD_TR_COMMENT;
                IntentUtil.openEditActivity(activity, AddEditTimeRegistrationsComment.class, timeRegistrationForContext);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_COMMENT: {
                trackerEvent = TrackerConstants.EventActions.EDIT_TR_COMMENT;
                IntentUtil.openEditActivity(activity, AddEditTimeRegistrationsComment.class, timeRegistrationForContext);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_EDIT_PROJECT_TASK: {
                trackerEvent = TrackerConstants.EventActions.EDIT_TR_PROJECT_AND_TASK;
                IntentUtil.openEditActivity(activity, EditTimeRegistrationProjectAndTask.class, timeRegistrationForContext);
                break;
            }
            case Constants.ContentMenuItemIds.TIME_REGISTRATION_RESTART: {
                trackerEvent = TrackerConstants.EventActions.RESTART_TIME_REGISTRATION;
                IntentUtil.openEditActivity(activity, EditTimeRegistrationRestart.class, timeRegistrationForContext);
                break;
            }
            default: {
                return false;
            }
        }

        if (trackerEvent != null) {
            String trackerEventSource = null;
            if (activity instanceof RegistrationDetailsActivity) {
                trackerEventSource = TrackerConstants.EventSources.REGISTRATION_DETAILS_ACTIVITY;
            } else if (activity instanceof TimeRegistrationsActivity) {
                trackerEventSource = TrackerConstants.EventSources.TIME_REGISTRATIONS_ACTIVITY;
            }

            if (trackerEventSource != null) {
                tracker.trackEvent(trackerEventSource, trackerEvent);
            }
        }

        return true;
    }
}