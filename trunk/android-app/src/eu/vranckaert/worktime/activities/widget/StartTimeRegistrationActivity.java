package eu.vranckaert.worktime.activities.widget;

import android.app.*;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.TaskByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.utils.notifications.NotificationBarManager;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import roboguice.activity.GuiceActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 23:25
 */
public class StartTimeRegistrationActivity extends GuiceActivity {
    private static final String LOG_TAG = StartTimeRegistrationActivity.class.getSimpleName();

    @Inject
    private WidgetService widgetService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Inject
    private TaskService taskService;

    private List<Task> availableTasks;

    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        Log.d(LOG_TAG, "Started the START TimeRegistration acitivity");

        Project selectedProject = projectService.getSelectedProject();
        if (Preferences.getSelectTaskHideFinished(getApplicationContext())) {
            availableTasks = taskService.findNotFinishedTasksForProject(selectedProject);
        } else {
            availableTasks = taskService.findTasksForProject(selectedProject);
        }
        Collections.sort(availableTasks, new TaskByNameComparator());

        if (availableTasks == null || availableTasks.size() == 0) {
            showDialog(Constants.Dialog.NO_TASKS_AVAILABLE);
        } else if (availableTasks.size() == 1) {
            Task task = availableTasks.get(0);
            createNewTimeRegistration(task);
        } else {
            showDialog(Constants.Dialog.CHOOSE_TASK);
        }
    }

    private void createNewTimeRegistration(final Task selectedTask) {
        removeDialog(Constants.Dialog.CHOOSE_TASK);

        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if(Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Date startTime = new Date();

                TimeRegistration newTr = new TimeRegistration();
                newTr.setTask(selectedTask);
                newTr.setStartTime(startTime);
                timeRegistrationService.create(newTr);

                tracker.trackEvent(
                        TrackerConstants.EventSources.START_TIME_REGISTRATION_ACTIVITY,
                        TrackerConstants.EventActions.START_TIME_REGISTRATION
                );

                projectService.refresh(selectedTask.getProject());
                NotificationBarManager notificationBarManager =
                            NotificationBarManager.getInstance(getApplicationContext());
                notificationBarManager.addOngoingTimeRegistrationMessage(
                        newTr.getTask().getProject().getName(),
                        newTr.getTask().getName()
                );

                widgetService.updateWidget(StartTimeRegistrationActivity.this);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE);
                Toast.makeText(StartTimeRegistrationActivity.this, R.string.msg_widget_time_reg_created, Toast.LENGTH_LONG).show();
                finish();
            }
        };
        threading.execute();
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE: {
                Log.d(LOG_TAG, "Creating loading dialog for starting a new time registration");
                dialog = ProgressDialog.show(
                        StartTimeRegistrationActivity.this,
                        "",
                        getString(R.string.lbl_widget_starting_new_timeregistration),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.CHOOSE_TASK: {
                List<String> tasks = new ArrayList<String>();
                for (Task task : availableTasks) {
                    tasks.add(task.getName());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.lbl_widget_title_select_task)
                       .setSingleChoiceItems(
                               StringUtils.convertListToArray(tasks),
                               -1,
                               new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        Log.d(LOG_TAG, "Task at index " + index + " choosen.");
                                        Task task = availableTasks.get(index);
                                        Log.d(LOG_TAG, "About to create a time registration for task with name " + task.getName());
                                        createNewTimeRegistration(task);
                                    }
                               }
                       )
                       .setOnCancelListener(new DialogInterface.OnCancelListener() {
                           public void onCancel(DialogInterface dialogInterface) {
                               Log.d(LOG_TAG, "No task choosen, close the activity");
                               StartTimeRegistrationActivity.this.finish();
                           }
                       });
                dialog = builder.create();
                break;
            }
            case Constants.Dialog.NO_TASKS_AVAILABLE: {
                AlertDialog.Builder alertNoTaskAvailable = new AlertDialog.Builder(this);
				alertNoTaskAvailable.setMessage(R.string.msg_no_tasks_available)
						   .setCancelable(false)
						   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   removeDialog(Constants.Dialog.NO_TASKS_AVAILABLE);
                                   StartTimeRegistrationActivity.this.finish();
                               }
                           });
				dialog = alertNoTaskAvailable.create();
                break;
            }
        };
        return dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}