/**************************************************************************
SystemWideTM
Copyright (C) 2020 Briac Pilpre

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/
package net.briac.omegat.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;

public class SystemWideTM
{
	public static final String PLUGIN_NAME = SystemWideTM.class.getPackage().getImplementationTitle();
	public static final String PLUGIN_VERSION = SystemWideTM.class.getPackage().getImplementationVersion();
	public static final ResourceBundle RES = ResourceBundle.getBundle("systemwide-tm", Locale.getDefault());
	private static final Logger LOGGER = Logger.getLogger(SystemWideTM.class.getName());

	private static final String TMX_LEVEL = OConsts.LEVEL1_TMX;
	private static final String IGNORE_EXT = ".ignore";
	private static final String SYSTEM_TM_LINK_NAME = "system";
	private static final File SYSTEM_TM_DIR = new File(StaticUtils.getConfigDir(), "tm");

	public static void loadPlugins()
	{
		LOGGER.info("Loading " + PLUGIN_NAME + " v." + PLUGIN_VERSION + "...");

		if (!SYSTEM_TM_DIR.exists()) {
			if (!SYSTEM_TM_DIR.mkdir()) {
				LOGGER.severe("Cannot create \"" + SYSTEM_TM_DIR + "\".");
				return;
			}
		}

		CoreEvents.registerProjectChangeListener(new IProjectEventListener()
		{
			private File projectTm;
			private File projectTmIgnored;

			@Override
			public void onProjectChanged(PROJECT_CHANGE_TYPE eventType)
			{
				if (eventType == PROJECT_CHANGE_TYPE.COMPILE) {
					copyGeneratedTM();
				} else if (eventType == PROJECT_CHANGE_TYPE.LOAD || eventType == PROJECT_CHANGE_TYPE.CREATE) {
					linkToSystemTm();
					disableCurrentProjectTM();
				} else if (eventType == PROJECT_CHANGE_TYPE.CLOSE) {
					reactivateCurrentProjectTM();
				}
			}

			private void reactivateCurrentProjectTM()
			{
				LOGGER.info("Reactivating current project TMX from system TM");

				if (projectTm != null && projectTmIgnored != null) {

					if (!projectTmIgnored.exists()) {
						return;
					}

					if (projectTm.exists()) {
						if (!projectTm.delete()) {
							LOGGER.warning("Couldn't delete " + projectTmIgnored);
						}
					}
					projectTmIgnored.renameTo(projectTm);
				}

			}

			private void disableCurrentProjectTM()
			{
				if (Core.getProject() == null || !Core.getProject().isProjectLoaded()) {
					return;
				}

				ProjectProperties config = Core.getProject().getProjectProperties();
				String fileName = config.getProjectName() + TMX_LEVEL + OConsts.TMX_EXTENSION;

				LOGGER.info("Disabling current project TMX from system TM");

				File languageSubfolder = getLanguageSubfolder(config);
				
				if (!languageSubfolder.exists()) {
					languageSubfolder.mkdirs();
				}

				projectTm = new File(languageSubfolder, fileName);
				projectTmIgnored = new File(languageSubfolder, fileName + IGNORE_EXT);

				if (!projectTm.exists()) {
					return;
				}

				if (projectTmIgnored.exists()) {
					if (!projectTmIgnored.delete()) {
						LOGGER.warning("Couldn't delete " + projectTmIgnored);
					}
				}
				projectTm.renameTo(projectTmIgnored);
			}

			private File getLanguageSubfolder(ProjectProperties config)
			{
				String sourceTargetLanguage = config.getSourceLanguage().getLanguageCode() + "-" + config.getTargetLanguage().getLanguageCode(); 
				
				return new File(SYSTEM_TM_DIR, sourceTargetLanguage);
			}

			private void copyGeneratedTM()
			{
				if (Core.getProject() == null) {
					return;
				}

				ProjectProperties config = Core.getProject().getProjectProperties();
				File projectTm = new File(config.getProjectRootDir(),
						config.getProjectName() + TMX_LEVEL + OConsts.TMX_EXTENSION);

				LOGGER.info("Copying genereated TMX to system TM");

				try {
					FileUtils.copyFile(projectTm, new File(getLanguageSubfolder(config), projectTm.getName() + IGNORE_EXT));
				} catch (IOException e) {
					LOGGER.severe("Cannot copy \"" + projectTm + "\" to \"" + SYSTEM_TM_DIR + "\".");
					e.printStackTrace();
				}
			}

			private void linkToSystemTm()
			{
				if (Core.getProject() == null) {
					return;
				}

				ProjectProperties config = Core.getProject().getProjectProperties();

				File projectSystemTM = new File(config.getTmDir().getAsFile(), SYSTEM_TM_LINK_NAME);

				if (projectSystemTM.exists()) {
					return;
				}

				Path link = Paths.get(projectSystemTM.toURI());
				Path target = Paths.get(getLanguageSubfolder(config).toURI());

				LOGGER.info("Creating symlink from \"" + link.toAbsolutePath() + "\" to \"" + target.toAbsolutePath()
						+ "\".");

				try {
					Files.createSymbolicLink(link, target);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		});

		CoreEvents.registerApplicationEventListener(new IApplicationEventListener()
		{
			@Override
			public void onApplicationStartup()
			{
				/* empty */
			}

			@Override
			public void onApplicationShutdown()
			{
				/* empty */
			}
		});
	}

	public static void unloadPlugins()
	{
		/* empty */
	}
}
