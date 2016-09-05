/**
 *
 */
package org.verapdf.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.verapdf.ReleaseDetails;
import org.verapdf.cli.commands.VeraCliArgParser;
import org.verapdf.pdfa.validation.ProfileDirectory;
import org.verapdf.pdfa.validation.Profiles;
import org.verapdf.pdfa.validation.ValidationProfile;

import java.io.IOException;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public final class VeraPdfCli {
	private static final String APP_NAME = "veraPDF";
	private static final String FLAVOURS_HEADING = APP_NAME + " supported PDF/A profiles:";
	private static final ProfileDirectory PROFILES = Profiles.getVeraProfileDirectory();

	private VeraPdfCli() {
		// disable default constructor
	}

	/**
	 * Main CLI entry point, process the command line arguments
	 *
	 * @param args
	 *            Java.lang.String array of command line args, to be processed
	 *            using Apache commons CLI.
	 */
	public static void main(final String[] args) {
		ReleaseDetails.addDetailsFromResource(
				ReleaseDetails.APPLICATION_PROPERTIES_ROOT + "app." + ReleaseDetails.PROPERTIES_EXT);
		VeraCliArgParser cliArgParser = new VeraCliArgParser();
		JCommander jCommander = new JCommander(cliArgParser);
		jCommander.setProgramName(APP_NAME);

		try {
			jCommander.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			showVersionInfo();
			jCommander.usage();
			System.exit(1);
		}
		if (cliArgParser.isHelp()) {
			showVersionInfo();
			jCommander.usage();
			System.exit(0);
		}
		messagesFromParser(cliArgParser);
		if (isProcess(cliArgParser)) {
			try {
				VeraPdfCliProcessor processor = VeraPdfCliProcessor.createProcessorFromArgs(cliArgParser);
				if (args.length == 0) jCommander.usage();
				processor.processPaths(cliArgParser.getPdfPaths(), args.length == 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void messagesFromParser(final VeraCliArgParser parser) {

		if (parser.listProfiles()) {
			listProfiles();
		}

		if (parser.showVersion()) {
			showVersionInfo();
		}
	}

	private static void listProfiles() {
		System.out.println(FLAVOURS_HEADING);
		for (ValidationProfile profile : PROFILES.getValidationProfiles()) {
			System.out.println("  " + profile.getPDFAFlavour().getId() + " - " + profile.getDetails().getName());
		}
		System.out.println();
	}

	private static void showVersionInfo() {
		ReleaseDetails details = ReleaseDetails.byId("gui");
		System.out.println("Version: " + details.getVersion());
		System.out.println("Built: " + details.getBuildDate());
		System.out.println(details.getRights());
		System.out.println();
	}

	private static boolean isProcess(final VeraCliArgParser parser) {
		if (parser.getPdfPaths().isEmpty() && (parser.isHelp() || parser.listProfiles() || parser.showVersion())) {
			return false;
		}
		return true;
	}
}
