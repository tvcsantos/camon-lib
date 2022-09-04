package pt.unl.fct.di.tsantos.util.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.View;
import org.tiling.scheduling.Scheduler;
import org.tiling.scheduling.SchedulerTask;
import pt.unl.fct.di.tsantos.util.ModifiableScheduleIterator;
import pt.unl.fct.di.tsantos.util.time.DateUtils;

public abstract class DefaultSingleFrameApplication
        extends SingleFrameApplication {

    protected File settingsDirectory;

    protected File savingDirectory;

    protected URL webLocation;

    protected String name;

    protected String shortName;

    protected Long updateInterval;

    protected SchedulerTask task;

    protected Scheduler scheduler;

    protected Date startTime;

    @Setting protected Date lastUpdate;

    protected Properties settings;

    protected ModifiableScheduleIterator msi;

    protected ImageIcon trayImageIcon;

    protected ImageIcon frameIcon;

    private static Map<Class<?>, Field[]> DATA_FIELD_CACHE
            = new HashMap<Class<?>, Field[]>();

    private static Map<Class<?>, Field[]> SETTING_FIELD_CACHE
            = new HashMap<Class<?>, Field[]>();

    protected boolean saveOnExit = false;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        initApplication();
        addExitListener(new ExitListener() {
            public boolean canExit(EventObject event) {
                saveOnExit = false;
                if (!canShutDown()) return false;
                if (checkChanges()) {
                    int status = JOptionPane.showConfirmDialog(getMainFrame(),
                    "Do you want to save changes before closing?", getName(),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    if (status == JOptionPane.YES_OPTION)
                        saveOnExit = true;
                    else if (status == JOptionPane.CANCEL_OPTION)
                        return false;
                } else saveOnExit = true;
                return true;
            }

            public void willExit(EventObject event) {
                
            }
        });
        /*createApplicationDirectory();
        populateApplicationDirectory();*/
        loadApplicationData();
        createSettingsDirectory();
        populateSettingsDirectory();
        loadData();
        initScheduler();
        showView();
    }
    
    @Override protected void shutdown() {
        preShutdown();
        super.shutdown();
        if (saveOnExit) {
            silentSaveApplicationData();
            silentSaveData();
        }
    }

    protected boolean checkChanges() {
        return false;
    }

    protected void preShutdown() {
        
    }

    protected boolean canShutDown() {
        return true;
    }
    
    protected void showView() {
        String className = this.getClass().getName();
        try {
            Class<?> c = Class.forName(
                    className.substring(0, className.length() - 3) + "View");
            Constructor<?> cons = null;
            try {
                cons = c.getConstructor(this.getClass());
            } catch (Exception ex) {
                cons = c.getConstructor(SingleFrameApplication.class);
            }
            show((View) cons.newInstance(this));
        } catch (Exception ex) {
            System.err.println(
                "Can't show using class reflection the main frame view."
                + " Override the method 'startup' and code it properly");
            ex.printStackTrace();
            System.exit(1);
        }        
    }

    protected void initApplication() {
        settingsDirectory = new File(AppUtils.USER_HOME,
                initSettingsDirectory());
        if (!settingsDirectory.exists()) settingsDirectory.mkdirs();
        savingDirectory = initSavingDirectory();
        if (!savingDirectory.exists()) savingDirectory.mkdirs();
        webLocation = initWebLocation();
        name = initName();
        shortName = initShortName();
        updateInterval = initUpdateInterval();
    }

    protected final File initSavingDirectory() {
        return new File(AppUtils.USER_HOME, initSettingsDirectory());
    }

    protected final void loadApplicationData() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(
                new File(getSettingsDirectory(), "appdata.db")));
            loadApplicationData(ois);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ois != null) try {
                ois.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected final void loadApplicationData(ObjectInputStream ois)
            throws Exception {
        /*Field[] fields = DATA_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Data.class);
            DATA_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            field.set(this, ois.readObject());
            if (!accessible) field.setAccessible(false);
        }*/
        savingDirectory = (File) ois.readObject();
    }

    public final void saveApplicationData() throws FileNotFoundException,
            IOException, IllegalArgumentException, IllegalAccessException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(
                    new File(getSettingsDirectory(), "appdata.db")));
            saveApplicationData(oos);
            //saveSettings();
        } finally {
            if (oos != null) oos.close();
        }
    }

    protected final void saveApplicationData(ObjectOutputStream oos)
            throws IOException,
            IllegalArgumentException, IllegalAccessException {
        oos.writeObject(savingDirectory);
        /*Field[] fields = DATA_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Data.class);
            DATA_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            final Object fieldValue = field.get(this);
            synchronized(fieldValue) {
                oos.writeObject(fieldValue);
            }
            if (!accessible) field.setAccessible(false);
        }*/
    }

    protected abstract void createSettingsDirectory();

    protected abstract void populateSettingsDirectory();

    protected final void loadSettings() {
        settings = new Properties();
        try {
            // load previous settings
            settings.load(new FileInputStream(
                    new File(getSavingDirectory(),
                                "settings.props")));
            loadSettingsFields(/*settings*/);
        } catch (Exception ex) {
            // exception so reinit settings
            ex.printStackTrace();
            settings = new Properties();
        }
    }

    protected final void loadSettingsFields(/*Properties settings*/)
            throws Exception {
        Field[] fields = SETTING_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Setting.class);
            SETTING_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            String property = settings.getProperty(field.getName());
            if (property != null) {
                Class<?> type = field.getType();
                if (type.isPrimitive()) {
                    if (type.equals(int.class))
                        field.set(this, Integer.parseInt(property));
                    else if (type.equals(boolean.class))
                        field.set(this, Boolean.parseBoolean(property));
                    else if (type.equals(double.class))
                        field.set(this, Double.parseDouble(property));
                    else if (type.equals(long.class))
                        field.set(this, Long.parseLong(property));
                    else if (type.equals(float.class))
                        field.set(this, Float.parseFloat(property));
                    else if (type.equals(short.class))
                        field.set(this, Short.parseShort(property));
                    else if (type.equals(byte.class))
                        field.set(this, Byte.parseByte(property));
                    else if (type.equals(char.class))
                        field.set(this, property.charAt(0));
                } else field.set(this,
                        importObjectProperty(property, field.getType()));
            }
            if (!accessible) field.setAccessible(false);
        }
    }

    protected final void updateSettings() 
            throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = SETTING_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Setting.class);
            SETTING_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            Object obj = field.get(this);
            if (obj != null)
                settings.setProperty(field.getName(), exportProperty(obj));
            else settings.remove(field.getName());
            if (!accessible) field.setAccessible(false);
        }
    }

    public Properties getSettings() {
        return settings;
    }

    protected final void saveSettings()
            throws FileNotFoundException, IOException,
            IllegalArgumentException, IllegalAccessException {
        updateSettings();
        settings.store(new FileOutputStream(
                    new File(getSavingDirectory(),
                        "settings.props")), getName());
    }

    protected final void silentSaveSettings() {
        try {
            saveSettings();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected final void silentSaveData() {
        try {
            saveData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected final void silentSaveApplicationData() {
        try {
            saveApplicationData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void initScheduler() {
        if (!isUpdatable()) return;
        scheduler = new Scheduler();
        task = new SchedulerTask() {
            @Override
            public void run() {
                try {
                    update();
                    lastUpdate = Calendar.getInstance().getTime();
                    //updateSettings();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        };
        startTime = Calendar.getInstance().getTime();
        if (lastUpdate == null ||
                DateUtils.difference(startTime, lastUpdate)
                > getUpdateInterval()) {
                msi = new ModifiableScheduleIterator(0, getUpdateInterval());
        } else {
            msi = new ModifiableScheduleIterator(getUpdateInterval(),
                    getUpdateInterval());
        }
        scheduler.schedule(task, msi);
    }

    protected abstract void update() throws Exception;

    public File getSavingDirectory() {
        return savingDirectory;
    }

    public File getSettingsDirectory() {
        return settingsDirectory;
    }

    public URL getWebLocation() {
        return webLocation;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Long getUpdateInterval() {
        return updateInterval;
    }

    protected abstract String initSettingsDirectory();

    protected abstract URL initWebLocation();

    protected abstract String initName();

    protected String initShortName() {
        return initName();
    }

    protected abstract Long initUpdateInterval();

    public final boolean isUpdatable() {
        return updateInterval != null;
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {}

    public ImageIcon getTrayImageIcon() {
        return trayImageIcon;
    }

    public void setTrayImageIcon(ImageIcon icon) {
        this.trayImageIcon = icon;
    }

    public ImageIcon getFrameIcon() {
        return frameIcon;
    }

    public void setFrameIcon(ImageIcon frameIcon) {
        this.frameIcon = frameIcon;
    }
    
    protected final void loadData() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(
                new File(getSavingDirectory(), "data.db")));
            loadData(ois);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ois != null) try {
                ois.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        loadSettings();
    }

    protected final void loadData(ObjectInputStream ois) throws Exception {
        Field[] fields = DATA_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Data.class);
            DATA_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            field.set(this, ois.readObject());
            if (!accessible) field.setAccessible(false);
        }
    }

    protected final <T> T importObjectProperty(String property, Class<T> type) {
        if (type.equals(String.class)) return type.cast(property);
        else if (type.equals(Integer.class)) {
            return type.cast(Integer.valueOf(property));
        } else if (type.equals(Boolean.class)) {
            return type.cast(Boolean.valueOf(property));
        } else if (type.equals(Double.class)) {
            return type.cast(Double.valueOf(property));
        } else if (type.equals(Long.class)) {
            return type.cast(Long.valueOf(property));
        } else if (type.equals(Float.class)) {
            return type.cast(Float.valueOf(property));
        } else if (type.equals(Short.class)) {
            return type.cast(Short.valueOf(property));
        } else if (type.equals(Byte.class)) {
            return type.cast(Byte.valueOf(property));
        } else if (type.equals(Character.class)) {
            return type.cast(Character.valueOf(property.charAt(0)));
        } else if (type.equals(Date.class)) {
            try {
                long number = Long.parseLong(property);
                return type.cast(new Date(number));
            } catch (NumberFormatException ex) {
                return importObjectPropertyExtended(property, type);
            }
        } else if (type.equals(File.class)) {
            return type.cast(new File(property));
        } else return importObjectPropertyExtended(property, type);
    }

    protected <T> T importObjectPropertyExtended(String property,
            Class<T> type) {
        throw new UnsupportedOperationException(type.toString());
    }

    protected final String exportProperty(Object obj) {
        if (obj instanceof String) {
            String string = (String) obj;
            return string;
        } else if (obj instanceof Date) {
            Date date = (Date) obj;
            return date.getTime() + "";
        } else if (obj instanceof File) {
            File file = (File) obj;
            return file.getAbsolutePath();
        }
        String result = exportPropertyExtended(obj);
        if (result != null) return result;
        return obj.toString();
    }

    protected String exportPropertyExtended(Object obj) {
        return null;
    }

    public static Field[] getAllFields(Class<?> aClass,
            Class<?> superClass) {
        List<Field> fields = new LinkedList<Field>();
        fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
        if (!aClass.equals(superClass)) {
            Class<?> iSuper = aClass.getSuperclass();
            if (iSuper != null) {
                fields.addAll(Arrays.asList(
                        getAllFields(iSuper, superClass)));
            }
        }
        return fields.toArray(new Field[]{});
    }

    public static Field[] getAllFields(Class<?> aClass,
            Class<?> superClass, Class<? extends Annotation> annotation) {
        List<Field> fields = new LinkedList<Field>();
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation))
                fields.add(field);
        }
        if (!aClass.equals(superClass)) {
            Class<?> iSuper = aClass.getSuperclass();
            if (iSuper != null) {
                fields.addAll(Arrays.asList(
                        getAllFields(iSuper, superClass, annotation)));
            }
        }
        return fields.toArray(new Field[]{});
    }

    public final void saveData() throws FileNotFoundException, 
            IOException, IllegalArgumentException, IllegalAccessException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(
                    new File(getSavingDirectory(), "data.db")));
            saveData(oos);
            saveSettings();
        } finally {
            if (oos != null) oos.close();
        }
    }

    protected final void saveData(ObjectOutputStream oos) throws IOException,
            IllegalArgumentException, IllegalAccessException {
        Field[] fields = DATA_FIELD_CACHE.get(getClass());
        if (fields == null) {
            fields = getAllFields(getClass(),
                DefaultSingleFrameApplication.class, Data.class);
            DATA_FIELD_CACHE.put(getClass(), fields);
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            final Object fieldValue = field.get(this);
            synchronized(fieldValue) {
                oos.writeObject(fieldValue);
            }
            if (!accessible) field.setAccessible(false);
        }
    }
}
