package com.komal.template_backend.model;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.List;
//
//@Document(collection = "privacy_policy")
//public class PrivacyPolicy {
//    @Id
//    private String id;
//    private String title;
//    private String effectiveDate;
//    private List<Section> content;
//    private Contact contact;
//
//    // getters & setters
//
//    public static class Section {
//        private String sectionTitle;
//        private List<String> body;
//        // getters & setters
//    }
//
//    public static class Contact {
//        private String email;
//        private String phone;
//        // getters & setters
//    }
//}
// build.gradle or pom.xml must include Lombok

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "privacy_policy")
public class PrivacyPolicy {
    @Id
    private String id;
    private String title;
    private String effectiveDate;
    private List<Section> content;
    private Contact contact;

    @Data
    public static class Section {
        private String sectionTitle;
        private List<String> body;
    }

    @Data
    public static class Contact {
        private String email;
        private String phone;
    }
}
