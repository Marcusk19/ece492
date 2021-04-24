import java.io.File;
import java.util.Vector;

public class PlayTheGame {
    public static void main(String[] args){
        try {
            Class<?> kidsGameInterfaceClassObject = Class.forName("KidsGameInterface");
            Class<?> teenGameInterfaceClassObject = Class.forName("TeenGameInterface");
        }
        catch(ClassNotFoundException cnfe){
            return;
        }
        File localDirectory = new File(System.getProperty("user.dir"));
        String[] listOfFiles = localDirectory.list();
        Vector<Class<?>> classFiles = new Vector<Class<?>>();
        for(String filename : listOfFiles){
            try {
                Class<?> classObject = Class.forName(filename);
                if(classObject.isInterface()) continue;
                classFiles.add(classObject);
            }
            catch(ClassNotFoundException cnfe){
                System.out.println(cnfe);
            }
            //if(PlayTheGame.class == classObject) continue;
        }
        if(args.length == 0) {
            System.out.println("List of available games");
            for(Class<?> classObject : classFiles){
                System.out.println(classObject.getName());
            }
        }
        /*
        for(Class<?> classObject : classFiles){
            Class<?>[] interfaceList = classObject.getInterfaces();
            for(Class<?> interfaceObject : interfaceList){
                if(kidsGameInterfaceClassObject == interfaceObject){}
                if(teenGameInterfaceClassObject == interfaceObject){}
            }
        } */

    }
}