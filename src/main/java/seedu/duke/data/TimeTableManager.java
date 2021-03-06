package seedu.duke.data;

import seedu.duke.exception.LessonInvalidTimeException;
import seedu.duke.exception.TimeTableInitialiseException;
import seedu.duke.data.ModuleManager.ModuleNotFoundException;
import seedu.duke.DukeLogger;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public class TimeTableManager {
    private static TimeTable timetable = new TimeTable();
    private static final DukeLogger logger = new DukeLogger(TimeTableManager.class.getName());
    
    /**
     * Initialise the semesterMap when it is empty.
     * Takes in the current week number in order to initialise.
     */
    public static void initialise(int userCurrWeekNum) throws TimeTableInitialiseException {
        logger.getLogger().info("Initialising timetable with user input: " + userCurrWeekNum);
        LocalDateTime now = LocalDateTime.now();
        // One sem has 13 weeks of lessons
        if (userCurrWeekNum > 0 && userCurrWeekNum < 15) {
            // e.g. Current week is 45, NUS week 2
            // semStartWeekNum is NUS week 1
            // therefore, semStartWeekNum = 45 - (2 - 1) = 44
            timetable.setSemStartWeekNum(now.get(ChronoField.ALIGNED_WEEK_OF_YEAR) - (userCurrWeekNum - 1));
        } else {
            logger.getLogger().warning("User input week num is invalid!");
            throw new TimeTableInitialiseException();
        }
        timetable.setSemEndWeekNum(timetable.getSemStartWeekNum() + 14);
        timetable.setSemRecessWeekNum(timetable.getSemStartWeekNum() + 6);
        // e.g. semStartWeekNum is 44, NUS week 1
        // Current week is NUS week 2
        // therefore, userCurrWeekNum = 44 + 2 - 1 = 45
        userCurrWeekNum = timetable.getSemStartWeekNum() + userCurrWeekNum - 1;
        logger.getLogger().info(String.format("Current, Start, Recess, End: %d, %d, %d, %d",
                now.get(ChronoField.ALIGNED_WEEK_OF_YEAR), timetable.getSemStartWeekNum(),
                timetable.getSemRecessWeekNum(), timetable.getSemEndWeekNum()));

        logger.getLogger().info("Filling semesterMap with empty LessonManagers");
        for (int weekNum = userCurrWeekNum; weekNum < timetable.getSemEndWeekNum(); weekNum++) {
            timetable.initWeek(weekNum);
        }
        logger.getLogger().info("Initialisation of timetable complete");
    }

    /**
     * Checks if semesterMap is initialised.
     *
     * @return
     *  Boolean of whether it is initialised
     */
    public static boolean isInitialised() {
        return !(timetable.countLessonManagers() == 0);
    }

    /**
     * Adds the lesson to semesterMap based on the repeat parameter. E.g. add to every week, every other week, etc.
     * Does not add to recess week (week 7) or reading week (week 15).
     * The repeat parameter where
     * 0: Current week only.
     * 1: Every week.
     * 2: Every even week.
     * 3: Every odd week.
     *
     * @param lesson The lesson object
     * @param repeat The repeat parameter
     * @throws LessonInvalidTimeException
     *  when the lesson to be added overlaps with an existing lesson
     */
    public static void addLesson(Lesson lesson, int repeat) throws LessonInvalidTimeException, ModuleNotFoundException {
        logger.getLogger().info("Adding lesson: " + lesson.toString() + " with repeat: " + repeat);
        if (!ModuleManager.contains(lesson.getModuleCode())) {
            logger.getLogger().warning("Could not add lesson as module not in user module list!");
            throw new ModuleNotFoundException();
        }
        int currWeek = getCurrWeekNum();
        logger.getLogger().info("Current week num is: " + currWeek);
        LessonManager lessonManager = timetable.getLessonManagerOfWeek(currWeek);
        lesson.generateHiddenId(); // tag lesson with unique ID for identifying when removing
        logger.getLogger().info("The lesson's hiddenId is: " + lesson.getHiddenId());

        // add for current week
        if (repeat == 0) {
            lessonManager.addLesson(lesson);
            return;
        }

        // add for every week, or alternate weeks
        for (; currWeek < timetable.getSemEndWeekNum(); currWeek++) { // don't add for reading week
            lessonManager = timetable.getLessonManagerOfWeek(currWeek);
            if (currWeek == timetable.getSemRecessWeekNum()) { // don't add for recess week
            } else if (repeat == 1) { // if repeat every week
                lessonManager.addLesson(lesson);
            } else if (isEvenWeek(currWeek) && repeat == 2) { // if repeat every even week
                lessonManager.addLesson(lesson);
            } else if (isOddWeek(currWeek) && repeat == 3) { // if repeat every odd week
                lessonManager.addLesson(lesson);
            }
        }
    }

    /**
     * Removes lesson by identifying the unique lesson from the current week, dayOfWeek, and lessonIndex.
     * Using the params, the lesson is found and the unique hiddenId is obtained.
     * Uses the removeLessonById method of LessonManager to remove all lessons with this hiddenId.
     *
     * @param dayOfWeek
     *  The day of week
     * @param lessonIndex
     *  The lesson index
     * @throws IndexOutOfBoundsException
     *  When the lesson index is out of bounds
     */
    public static void removeLesson(DayOfWeek dayOfWeek, int lessonIndex) throws IndexOutOfBoundsException {
        logger.getLogger().info("Removing lesson with index " + lessonIndex + " on " + dayOfWeek
                + " (failed if no successful removal log below)");
        int currWeek = getCurrWeekNum();

        // retrieve the unique ID, as index does not stay constant over different weeks
        LessonManager lessonManager = timetable.getLessonManagerOfWeek(currWeek);
        Lesson lesson = lessonManager.getDayLessonList(dayOfWeek).get(lessonIndex);
        String id = lesson.getHiddenId();

        // remove by ID
        timetable.removeLessonById(dayOfWeek, id);
        logger.getLogger().info("Successful removal of lesson with hiddenId " + id);
    }

    /**
     * Get an ArrayList of lessons happening on a certain day.
     *
     * @param dayOfWeek
     *  The specific day of week
     * @return
     *  The ArrayList of lessons
     */
    public static ArrayList<Lesson> getSpecificDayLessons(DayOfWeek dayOfWeek) {
        int currWeek = getCurrWeekNum();
        LessonManager lessonManager = timetable.getLessonManagerOfWeek(currWeek);
        return lessonManager.getDayLessonList(dayOfWeek);
    }

    /**
     * Get an ArrayList of ArrayLists of lessons happening in the specified week.
     * Outer ArrayList: Holds one ArrayList for each day of week, starting from Monday.
     * Inner ArrayList: Holds lessons happening on that day of week.
     *
     * @return
     *  The ArrayList of ArrayList of lessons
     */
    public static ArrayList<ArrayList<Lesson>> getSpecifiedWeekLessons(int currWeek) {
        ArrayList<ArrayList<Lesson>> outputList = new ArrayList<>();
        LessonManager lessonManager = timetable.getLessonManagerOfWeek(currWeek);
        for (DayOfWeek eachDay : DayOfWeek.values()) {
            outputList.add(lessonManager.getDayLessonList(eachDay));
        }
        return outputList;
    }

    /**
     * Get the current week number (of the year).
     *
     * @return
     *  The current week number.
     */
    public static int getCurrWeekNum() {
        LocalDateTime now = LocalDateTime.now();
        return now.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }

    /**
     * Checks whether the current NUS week is odd.
     *
     * @return
     *  Whether current NUS week is odd.
     */
    public static boolean isOddWeek(int week) {
        int recessWeek = timetable.getSemRecessWeekNum();
        int startWeek = timetable.getSemStartWeekNum();
        if (week < recessWeek) {
            return (week - startWeek + 1) % 2 == 1; // check if the NUS week is even
        } else if (week == recessWeek) {
            return false;
        } else {
            return (week - startWeek + 1) % 2 == 0; // check if the NUS week is even (accounting for recess week)
        }
    }

    /**
     * Checks whether the current NUS week is even.
     *
     * @return
     *  Whether current NUS week is even.
     */
    public static boolean isEvenWeek(int week) {
        int recessWeek = timetable.getSemRecessWeekNum();
        int startWeek = timetable.getSemStartWeekNum();
        if (week < recessWeek) {
            return (week - startWeek + 1) % 2 == 0; // check if the NUS week is even
        } else if (week == recessWeek) {
            return false;
        } else {
            return (week - startWeek + 1) % 2 == 1; // check if the NUS week is even (accounting for recess week)
        }
    }

    public static int getWeekLessonCount(int week) {
        return timetable.countWeekLessons(week);
    }

    public static int getTimetableLessonCount() {
        return timetable.countTimetableLessons();
    }

    public static TimeTable getTimeTable() {
        return timetable;
    }

    public static void loadTimeTable(TimeTable loadedTimeTable) {
        timetable = loadedTimeTable;
    }

    public static void clearTimeTable() {
        timetable = new TimeTable();
    }

    /*
    public static int getSemEndWeekNum() {
        return semEndWeekNum;
    }

    public static int getSemStartWeekNum() {
        return semStartWeekNum;
    }

    public static int getSemRecessWeekNum() {
        return semRecessWeekNum;
    }

    public static boolean isSemRecessWeek(int week) {
        return week == semRecessWeekNum;
    }
     */
}
