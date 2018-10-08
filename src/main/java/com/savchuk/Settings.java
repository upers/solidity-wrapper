package com.savchuk;

import java.io.File;
import java.util.List;

public class Settings {
    public static String importStr = "import '";
    public static String nodeModulesFolderName = "node_modules";
    public static String contractsFolderName = "contracts";
    public static String truffleDir;
    public static String contractsDir;
    public static String binDir;
    public static String abiDir;
    public static String javaDir;
    public static String wrappersPackage;
    public static List<String> contractsNeedToWrapper;
    public static List<String> nodeModulesNames;

    private Settings() {}
}
