package com.savchuk;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.util.*;
import java.util.stream.Collectors;

import static com.savchuk.Settings.*;

public class Main {

    public static void main(String[] args) throws IOException {
        new Configurator().initSettings(args);

        List<Path> solcFiles = Files.walk(Paths.get(contractsDir)).filter(path -> {
            if (Files.isDirectory(path))
                return false;

            return true;
        }).collect(Collectors.toList());

        Map<Path, byte[]> nativeSources = new HashMap<>();
        for (Path path : solcFiles) {
            try {
                byte[] bytes = Files.readAllBytes(path);
                nativeSources.put(path,  bytes);
                    System.out.println(path.toString());
                    List<String> lines = Files.readAllLines(path);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        for (String moduleName : nodeModulesNames) {
                            if (line.matches("^import\\W+(\"|\')" + moduleName + ".*")) {
                                int start = line.indexOf(moduleName);
                                String begin = line.substring(0, start);
                                String insert = createPrefixForImport(path);
                                String finish = line.substring(start, line.length());

                                String newLine = begin + insert + finish;

                                lines.set(i, newLine);
                            }
                        }
                    }

                    Files.write(path, lines, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            compileBinaryAndAbi();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Path path : nativeSources.keySet()) {
            try {
                Files.write(path, nativeSources.get(path));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static String createPrefixForImport(Path path) {
        Path nativePath = path;
        String prefix = nodeModulesFolderName;
        while (true) {
            prefix = ".." + File.separator + prefix;
            path = path.getParent();
            if (path == null)
                throw new IllegalStateException(
                        "File: " + nativePath.toString() + " is not in " + contractsFolderName + " folder");

            if (path.getFileName().toString().equalsIgnoreCase(contractsFolderName))
                break;
        }

        return prefix + File.separator;
    }

    public static void compileBinaryAndAbi() throws IOException, InterruptedException {
        String command = "solc --allow-paths ";
        for (int i = 0; i < nodeModulesNames.size(); i++) {
            String moduleName = nodeModulesNames.get(i);
            command += truffleDir + File.separator + nodeModulesFolderName + File.separator + moduleName + File.separator + contractsFolderName;
            if (nodeModulesNames.size() - 1 == i)
                command += " ";
            else
                command += ",";
        }

        command += "--bin --abi --overwrite --optimize -o " + binDir;

        List<Path> solFiles = Files.walk(Paths.get(contractsDir)).filter(path -> {
            if (path.getFileName().toString().matches(".*\\.sol"))
                return true;

            return false;
        }).collect(Collectors.toList());

        for (Path solcFile : solFiles) {
            compile(command, solcFile);
            wrap(solcFile);
        }
    }

    private static void compile(String command, Path solcFile) throws IOException, InterruptedException {
        command += " " + solcFile.toString();
        System.out.println("Compile command: " + command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    private static void wrap(Path solcFile) throws InterruptedException, IOException {
        String baseName = FilenameUtils.getBaseName(solcFile.getFileName().toString());
        if (!contractsNeedToWrapper.contains(baseName))
            return;

        String command = "web3j solidity generate --javaTypes " + binDir + File.separator + baseName + ".bin";
        command += " " + abiDir + File.separator + baseName + ".abi" + " -o " + javaDir +" -p " + wrappersPackage;
        System.out.println("Wrap command: " + command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }



}
