package com.savchuk;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.savchuk.Settings.truffleDir;

public class Configurator {

    private String optionalParNodeModules = "-n";

    private int truffleDirIndex = 0;

    private int javaDirIndex = 1;

    private int javaPackageIndex = 2;

    private int contractNamesIndex = 3;

    private int offset = 0;

    //Truffle Dir, Java dir, package, node_modules, Contracts Names,
    public void initSettings(String[] args) {
        if (args.length < 4) {
            System.out.println("Not correct arguments amount");
            System.exit(-1);
        }


        int nodeModulesIndex = indexOfParameter(args, optionalParNodeModules);
        for (int index = 0; index < args.length; index++) {
            if (nodeModulesIndex == index) {
                index++;
                offset += 2;
                Settings.nodeModulesNames = argsAsList(args[index]);
                continue;
            }

            if (truffleDirIndex + offset == index) {
                Settings.truffleDir = args[index];
                Settings.contractsDir = Settings.truffleDir + File.separator + "contracts";
                Settings.binDir = Settings.truffleDir + File.separator + "build" + File.separator + "bin";
                Settings.abiDir = Settings.truffleDir + File.separator + "build" + File.separator + "bin";

                if (!Files.exists(Paths.get(Settings.truffleDir))) {
                    System.out.println("No such directory: " + Settings.truffleDir);
                    System.exit(-1);
                }

                File bDir = new File(Settings.binDir);
                File aDir = new File(Settings.abiDir);
                if (!bDir.exists())
                    bDir.mkdirs();
                if (!aDir.exists())
                    aDir.mkdirs();
            } else if (javaDirIndex + offset == index) {
                Settings.javaDir = args[index];
            } else if (javaPackageIndex + offset == index) {
                Settings.wrappersPackage = args[index];
            } else if (contractNamesIndex + offset == index) {
                Settings.contractsNeedToWrapper = argsAsList(args[index]);
            }
        }
    }

    private List argsAsList(String param) {
        return Arrays.stream(param.split(",")).map(String::trim).collect(Collectors.toList());

    }

    private int indexOfParameter(String[] args, String par) {
        for (int i = 0; i < args.length; i++) {
            String val = args[i];
            if (val.equalsIgnoreCase(par))
                return i;
        }

        return -1;
    }
}
