package ch.welld.drools;

import static ch.welld.drools.FileUtils.createDirectoryIfNotExists;
import static ch.welld.drools.FileUtils.findAllFilesWithExtensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "convert-knowledge", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class KnowledgeConverterMojo extends AbstractMojo {
    @Parameter(property = "outputDirectory", required = true)
    private File outputDirectory;

    @Parameter(property = "inputDirectory", required = true)
    private File inputDirectory;

    @Parameter(defaultValue = "false", property = "overwriteFiles")
    private boolean overwriteFiles;

    private void logParameters() {
        getLog().info("--- PARAMETERS ---");
        getLog().info("Output directory: " + outputDirectory);
        getLog().info("Input directory: " + inputDirectory);
        getLog().info("--- ---------- ---");
    }

    private void convertToDrlAndCopy(Path knowledgeFilePath, Path destinationPath, boolean overwriteFiles) {
        try {
            getLog().debug("Copying file " + knowledgeFilePath);
            DroolsConverter.copyKnowledge(
                    knowledgeFilePath.toFile(),
                    destinationPath,
                    overwriteFiles
            );
        } catch (IOException ex) {
            getLog().error("Cannot convert file " + knowledgeFilePath.toString());
        }
    }

    @Override
    public void execute() {
        logParameters();

        Path inputDirectoryPath = inputDirectory.toPath();
        Path outputDirectoryPath = outputDirectory.toPath();

        createDirectoryIfNotExists(outputDirectoryPath);

        try {
            List<Path> knowledgeFiles = findAllFilesWithExtensions(inputDirectory, Set.of(".gdst", ".drl"));
            getLog().info("Knowledge files found: " + knowledgeFiles.size());
            knowledgeFiles.forEach(knowledgeFile -> getLog().debug(" - " + knowledgeFile));

            /* Convert all found knowledge files into drl files */
            knowledgeFiles.forEach(knowledgeFilePath -> {
                convertToDrlAndCopy(
                    knowledgeFilePath,
                    outputDirectoryPath.resolve(
                        inputDirectoryPath.relativize(knowledgeFilePath.getParent())
                    ),
                    overwriteFiles
                );
            });
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
