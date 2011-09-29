package eu.vranckaert.worktime.constants;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 15:22
 */
public class Constants {
    public class Dialog {
        public static final int DELETE_PROJECT_YES_NO = 0;
        public static final int CHOOSE_EXPORT_FILE_TYPE = 3;
        public static final int CHOOSE_EXPORT_CSV_SEPARATOR = 4;
        public static final int LOADING_TIMEREGISTRATIONS_EXPORT = 5;
        public static final int DELETE_TIME_REGISTRATION_YES_NO = 6;
        public static final int EXPORT_UNAVAILABLE = 7;
        public static final int CHOOSE_SELECTED_PROJECT = 8;
        public static final int LOADING_TIMEREGISTRATION_CHANGE = 9;
        public static final int CHOOSE_TASK = 10;
        public static final int NO_TASKS_AVAILABLE = 11;
        public static final int DELETE_TASK_YES_NO = 12;
        public static final int DELETE_TIME_REGISTRATIONS_OF_TASK_YES_NO = 13;
        public static final int WARN_PROJECT_DELETE_PROJECT_STILL_IN_USE = 14;
        public static final int ENTER_COMMENT_FOR_TR = 15;
        public static final int END_TIME_REGISTRATION_YES_NO = 16;
        public static final int CHOOSE_DATE = 17;
        public static final int CHOOSE_TIME = 18;
        public static final int VALIDATION_DATE_LOWER_LIMIT = 19;
        public static final int VALIDATION_DATE_HIGHER_LIMIT = 20;
        public static final int WARN_TASK_NOT_FINISHED_ONGOING_TR = 21;
        public static final int ASK_FINISH_TASK = 22;
        public static final int BACKUP_IN_PROGRESS = 23;
        public static final int BACKUP_SUCCESS = 24;
        public static final int BACKUP_ERROR = 25;
        public static final int BACKUP_RESTORE_FILE_SEARCH_NOTHING_FOUND = 26;
        public static final int BACKUP_RESTORE_FILE_SEARCH_SHOW_LIST = 27;
        public static final int BACKUP_RESTORE_FILE_SEARCH_NO_SD = 28;
        public static final int RESTORE_IN_PROGRESS = 29;
        public static final int BACKUP_RESTORE_START_QUESTION = 30;
        public static final int RESTORE_SUCCESS = 31;
        public static final int RESTORE_ERROR = 32;
        public static final int BACKUP_RESTORE_DOCUMENTATION = 33;
        public static final int REPORTING_CRITERIA_SELECT_PROJECT = 34;
        public static final int REPORTING_CRITERIA_SELECT_TASK = 35;
        public static final int REPORTING_CRITERIA_SELECT_START_DATE = 36;
        public static final int REPORTING_CRITERIA_SELECT_END_DATE = 37;
        public static final int REPORTING_CRITERIA_SELECT_END_DATE_ERROR_BEFORE_START_DATE = 38;
        public static final int LOADING_REPORTING_RESULTS = 39;
        public static final int TIMEREGISTRATIONS_EXPORT_FILE_EXPORT_DONE = 40;
    }
    public class IntentRequestCodes {
        public static final int ADD_PROJECT = 0;
        public static final int ADD_TASK = 1;
        public static final int EDIT_PROJECT = 2;
        public static final int EDIT_TASK = 3;
        public static final int PROJECT_DETAILS = 4;
        public static final int REGISTRATION_DETAILS = 5;
        public static final int STOP_TIME_REGISTRATION = 6;
        public static final int REGISTRATION_EDIT_DIALOG = 7;
    }
    public class Preferences {
        public static final String PREFERENCES_NAME = "WorkTime_0001";

        public static final String EXPORT_TIME_REG_FILE_NAME_DEFAULT_VALUE = "export";
        public static final int SELECTED_PROJECT_ID_DEFAULT_VALUE = -1;
        public static final boolean WIDGET_ENDING_TIME_REGISTRATION_COMMENT_PREFERENCE_DEFAULT_VALUE = true;
        public static final boolean WIDGET_ENDING_TIME_REGISTRATION_FINISH_TASK_PREFERENCE_DEFAULT_VALUE = false;
        public static final boolean SHOW_STATUS_BAR_NOTIFICATIONS_PREFERENCE_DEFAULT_VALUE = true;
        public static final int WIDGET_ENDING_TIME_REGISTRATION_COMMENT_MAX_HISTORY_STORAGE_PREFERENCE_DEFAULT_VALUE = 5;
        public static final String DISPLAY_HOUR_12_24_FORMAT_DEFAULT_VALUE = "system-default";
        public static final boolean SELECT_TASK_HIDE_FINISHED_DEFAULT_VALUE = true;
        public static final boolean DISPLAY_TASKS_HIDE_FINISHED_DEFAULT_VALUE = false;
        public static final String WEEK_STARTS_ON_DEFAULT_VALUE = "7";

        public class Keys {
            public static final String EXPORT_TIME_REG_FILE_NAME = "exportFileName";
            public static final String EXPROT_TIME_REG_FILE_TYPE = "exportFileType";
            public static final String EXPROT_TIME_REG_CSV_SEPARATOR = "exportCsvSeperator";
            public static final String SELECTED_PROJECT_ID = "selectedProjectId";
            public static final String WIDGET_ENDING_TIME_REGISTRATION_COMMENT_PREFERENCE = "widgetEndingTimeRegistrationCommentPreference";
            public static final String WIDGET_ENDING_TIME_REGISTRATION_FINISH_TASK_PREFERENCE = "widgetEndingTimeRegistrationFinishTaskPreference";
            public static final String WIDGET_ENDING_TIME_REGISTRATION_COMMENT_MAX_HISTORY_STORAGE_PREFERENCE = "widgetEndingTimeRegistrationCommentMaxHistoryStoragePreference";
            public static final String SHOW_STATUS_BAR_NOTIFICATIONS_PREFERENCE = "showStatusBarNotificationsPreference";
            public static final String DISPLAY_HOUR_12_24_FORMAT = "displayHour1224Format";
            public static final String DISPLAY_TASKS_HIDE_FINISHED = "displayTasksHideFinished";
            public static final String SELECT_TASK_HIDE_FINISHED = "selectTaskHideFinished";
            public static final String WEEK_STARTS_ON = "weekStartsOn";
        }
    }
    public class Export {
        public static final String EXPORT_DIRECTORY = "worktime";
        public static final String BACKUP_DIRECTORY = "worktime-backup";
    }
    public class Extras {
        public static final String PROJECT = "project";
        public static final String TASK = "task";
        public static final String TIME_REGISTRATION = "timeRegistration";
        public static final String TIME_REGISTRATION_NEXT = "timeRegistrationNext";
        public static final String TIME_REGISTRATION_PREVIOUS = "timeRegistrationPrevious";
        public static final String TIME_REGISTRATION_START_DATE = "timeRegistrationStartDate";
        public static final String TIME_REGISTRATION_END_DATE = "timeRegistrationEndDate";
    }
    public class ContentMenuItemIds {
        public static final int PROJECT_DETAILS = 1;
        public static final int PROJECT_DELETE = 2;
        public static final int PROJECT_ADD = 3;
        public static final int PROJECT_EDIT = 4;
        public static final int TASK_EDIT = 5;
        public static final int TASK_ADD = 6;
        public static final int TASK_DELETE = 7;
        public static final int TIME_REGISTRATION_DETAILS = 8;
        public static final int TIME_REGISTRATION_EDIT_START = 9;
        public static final int TIME_REGISTRATION_EDIT_END = 10;
        public static final int TIME_REGISTRATION_EDIT_COMMENT = 11;
        public static final int TIME_REGISTRATION_EDIT_PROJECT_TASK = 12;
        public static final int TASK_MARK_UNFINISHED = 13;
        public static final int TASK_MARK_FINISHED = 14;
        public static final int TIME_REGISTRATION_DELETE = 15;
        public static final int TIME_REGISTRATION_RESTART = 16;
        public static final int TIME_REGISTRATION_ADD_COMMENT = 17;
    }
}