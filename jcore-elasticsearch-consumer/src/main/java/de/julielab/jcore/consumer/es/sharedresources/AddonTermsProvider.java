package de.julielab.jcore.consumer.es.sharedresources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.julielab.jcore.utility.JCoReTools;
import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddonTermsProvider implements IAddonTermsProvider {
	Logger log = LoggerFactory.getLogger(AddonTermsProvider.class);

	private Map<String, String[]> addonTerms;
	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		try {
			addonTerms = new HashMap<>();
			log.info("Loading addon terms from " + aData.getUri());
			int addons = 0;
			InputStream inputStream;
			try {
				inputStream = JCoReTools.resolveExternalResourceGzipInputStream(aData);
			} catch (Exception e) {
				throw new IOException("Could not read from " + aData.getUri() + ": Resource not found.");
			}
			List<String> addonLines = IOUtils.readLines(inputStream, "UTF-8");
			for (String line : addonLines) {
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;
				String[] mapping = line.split("\t");
				if (mapping.length != 2)
					throw new IllegalArgumentException("Format problem with addon terms line " + line + ": Not exactly one tab character.");
				// we use internalization to reduce memory
				// requirements
				String term = mapping[0].trim().intern();
				String[] addonArray = mapping[1].split("\\|");
				for (int i = 0; i < addonArray.length; i++) {
					String trimmedAddon = addonArray[i].trim();
					// we use internalization to reduce memory
					// requirements
					addonArray[i] = trimmedAddon.intern();
					addons++;
				}
				addonTerms.put(term, addonArray);
			}
			log.info("Loaded {} addons for {} terms.", addons, addonTerms.size());
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public Map<String, String[]> getAddonTerms() {
		return addonTerms;
	}

}
