package edu.harvard.iq.dataverse_hub.controller.scheduled;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.iq.dataverse_hub.model.Installation;
import edu.harvard.iq.dataverse_hub.service.InstallationService;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Component
public class InstallationGitImporter {

    private static final String INSTALLATIONS_URL = "https://raw.githubusercontent.com/IQSS/dataverse-installations/refs/heads/main/data/data.json";

    @Autowired
    private InstallationService installationService;

    @Scheduled(fixedRate = 3600000)
    public void importInstallations() {
        System.out.println("Importing installations...");
        
        
        RestTemplate restTemplate = new RestTemplate();
        String jsonImport = restTemplate.getForObject(INSTALLATIONS_URL, String.class); 

        try {
            ObjectMapper mapper = new ObjectMapper();
            GitHubInstallationWrapper installations = mapper.readValue(jsonImport, GitHubInstallationWrapper.class);

            List<Installation> dtos = InstallationGitImporter.transform(installations);
            for (Installation installation : dtos) {
                Installation existingInstallation = installationService.findByName(installation.getName());
                if (existingInstallation == null) {
                    installation.setDvHubId(new Date().toString() + installation.getName());
                    installationService.save(installation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Installation transform(InstallationWrapper installationWrapper) {
        Installation installation = new Installation();
        installation.setDvHubId("");
        installation.setName(installationWrapper.name);
        installation.setDescription(installationWrapper.description);
        installation.setLatitude(installationWrapper.latitude);
        installation.setLongitude(installationWrapper.longitude);
        installation.setHostname(installationWrapper.hostname); 
        installation.setCountry(installationWrapper.country);
        installation.setContinent(installationWrapper.continent);
        installation.setLaunchYear(installationWrapper.launchYear);
        installation.setGdccMember(installationWrapper.gdccMember);
        installation.setDoiAuthority(installationWrapper.doiAuthority);
        installation.setContactEmail(installationWrapper.contactEmail);
        return installation;
    }

    private static List<Installation> transform(GitHubInstallationWrapper gitHubInstallationWrapper) {
        List<Installation> installations = new ArrayList<Installation>();
        for (InstallationWrapper installationWrapper : gitHubInstallationWrapper.installations) {
            installations.add(transform(installationWrapper));
        }

        return installations;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubInstallationWrapper {
        @JsonProperty("installations")
        List<InstallationWrapper> installations;
    
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class InstallationWrapper {

        @JsonProperty("name")
        private String name;

        @JsonProperty("url")
        private String url;

        @JsonProperty("dataverseVersion")
        private String dataverseVersion;

        @JsonProperty("lat")
        private String latitude;

        @JsonProperty("lng")
        private String longitude;

        @JsonProperty("clientInstitutionId")
        private String clientInstitutionId;

        @JsonProperty("continent")
        private String continent;

        @JsonProperty("country")
        private String country;

        @JsonProperty("additionalContactInformation")
        private String additionalContactInformation;

        @JsonProperty("notes")
        private String notes;

        @JsonProperty("description")
        private String description;

        @JsonProperty("hostname")
        private String hostname;

        @JsonProperty("launch_year")
        private Integer launchYear;

        @JsonProperty("gdcc_member")
        private boolean gdccMember;

        @JsonProperty("doi_authority")
        private String doiAuthority;

        @JsonProperty("contact_email")
        private String contactEmail;
    
    }

}