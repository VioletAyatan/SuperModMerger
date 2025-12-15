package ankol.mod.merger;

import java.io.IOException;

public class AppMain {
    public static void main(String[] args) {
        try {
            MergeConfig config = MergeConfig.fromArgs(args);
            config.validate();

            if (config.verbose) {
                System.out.println("Config: " + config);
            }

            ModMerger merger = new ModMerger(
                config.mod1Directory,
                config.mod2Directory,
                config.outputDirectory,
                config.interactiveMode,
                config.defaultMergeStrategy
            );

            merger.merge();

            System.out.println("\nDone!");
            System.exit(0);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);

        } finally {
            ConflictResolver.close();
        }
    }
}

