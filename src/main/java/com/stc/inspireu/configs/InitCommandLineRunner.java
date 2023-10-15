package com.stc.inspireu.configs;

import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.InitDataService;
import com.stc.inspireu.utils.PasswordUtil;
import com.stc.inspireu.utils.RoleName;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@Component
class InitCommandLineRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Value("${superadmin.email}")
    private String superadminEmail;

    @Value("${superadmin.password}")
    private String pass;

    @Autowired
    private RevenueModelRepository revenueModelRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private SegmentRepository segmentRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private InitDataService initDataService;

    @Autowired
    private AcademyRoomRepository academyRoomRepository;

    @Autowired
    private ReasonDropdownRepository reasonDropdownRepository;

    @Autowired
    private OneToOneMeetingRepository oneToOneMeetingRepository;

    @Autowired
    private IntakeProgramRepository intakeProgramRepository;

    @Autowired
    private DueDiligenceTemplate2021Repository dueDiligenceTemplate2021Repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private KeyValueRepository keyValueRepository;

    @Value("${minio.bucket}")
    private String minioBucket;

    @Override
    public void run(String... args) throws Exception {
        try {
            boolean bucketFound = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
            if (bucketFound) {
                LOGGER.info("minio " + minioBucket + " exists");
            } else {
                LOGGER.info("minio " + minioBucket + " does not exist");

                try {
                    LOGGER.info("creating minio bucket" + minioBucket + " with default settings");

                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }

            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage() + " minio server");
        }

        long c = roleRepository.count();

        if (c == 0) {
            Field[] fields = RoleName.class.getDeclaredFields();

            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    RoleName roleName = new RoleName();
                    Role role = new Role();
                    role.setRoleName(f.getName());
                    role.setDescription(f.getName());
                    role.setRoleAlias(RoleName.getAlias((String) f.get(roleName)));
                    role.setWillManagement(f.getName().contains("STARTUPS") ? false : true);
                    role.setUrlScope(f.getName().contains("STARTUPS") ? (f.getName().contains("BENEFICIARY") ? "/beneficiary/**" : "/startups/**") : "/management/**");
                    roleRepository.save(role);
                }
            }

        }

        User user = userRepository.findByEmail(superadminEmail);

        if (user == null) {

            initDataService.createSuperAdmin(superadminEmail, pass);
        }

        long rmc = revenueModelRepository.count();

        if (rmc == 0) {

            RevenueModel r1 = new RevenueModel("Sales");
            RevenueModel r2 = new RevenueModel("Subscription");
            RevenueModel r3 = new RevenueModel("Freemium");
            RevenueModel r4 = new RevenueModel("Ad-Based");
            RevenueModel r5 = new RevenueModel("Commission");
            RevenueModel r6 = new RevenueModel("Others");

            List<RevenueModel> list1 = Arrays.asList(r1, r2, r3, r4, r5, r6);

            revenueModelRepository.saveAll(list1);
        }

        long sc = statusRepository.count();

        if (sc == 0) {
            Status status = new Status();
            status.setStatus("status1");
            statusRepository.save(status);
        }

        long sgc = segmentRepository.count();

        if (sgc == 0) {

            Segment s1 = new Segment("Loyalty Programs");
            Segment s2 = new Segment("Content");
            Segment s3 = new Segment("Analytics/Big Data");
            Segment s4 = new Segment("Sports");
            Segment s5 = new Segment("Automotive");
            Segment s6 = new Segment("Medical");
            Segment s7 = new Segment("Social Networking");
            Segment s8 = new Segment("IOT");
            Segment s9 = new Segment("Travel and Tourism");
            Segment s10 = new Segment("Health/Fitness");
            Segment s11 = new Segment("Game/Entertainment");
            Segment s12 = new Segment("Fintech");
            Segment s13 = new Segment("Artificial Intelligence(AI)");
            Segment s14 = new Segment("Advertising/Marketing");
            Segment s15 = new Segment("Education");
            Segment s16 = new Segment("Logistics");
            Segment s17 = new Segment("Enterprise");
            Segment s18 = new Segment("eCommerce");
            Segment s19 = new Segment("Delivery Services");
            Segment s20 = new Segment("Cybersecurity");
            Segment s21 = new Segment("Others");

            List<Segment> list1 = Arrays.asList(s1, s2, s3, s4, s5, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15,
                s16, s17, s18, s19, s20, s21);

            segmentRepository.saveAll(list1);
        }

        long c12 = reasonDropdownRepository.count();

        if (c12 == 0) {
            ReasonDropdown rd1 = new ReasonDropdown("Reason1");
            ReasonDropdown rd2 = new ReasonDropdown("Reason2");
            ReasonDropdown rd3 = new ReasonDropdown("Reason3");
            List<ReasonDropdown> ls = Arrays.asList(rd1, rd2, rd3);
            reasonDropdownRepository.saveAll(ls);
        }
        initDataService.createEmailTemplates();

        KeyValue midnightCronLock = keyValueRepository.findByKeyName(Constant.MIDNIGHT_CRON_LOCK.toString());

        if (midnightCronLock == null) {
            KeyValue e = new KeyValue();
            e.setDescription("lock for midnight cron job");
            e.setKeyName(Constant.MIDNIGHT_CRON_LOCK.toString());
            e.setValueName(Constant.UNLOCKED.toString());
            keyValueRepository.save(e);
        }

        KeyValue totalStartups = keyValueRepository.findByKeyName(Constant.TOTAL_STARTUPS_INCUBATED.toString());

        if (totalStartups == null) {
            KeyValue e = new KeyValue();
            e.setDescription("count of incubated startups");
            e.setKeyName(Constant.TOTAL_STARTUPS_INCUBATED.toString());
            e.setValueName("0");
            keyValueRepository.save(e);
        }

        KeyValue filesAllowded = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());

        if (filesAllowded == null) {
            KeyValue e = new KeyValue();
            e.setDescription("files allowded");
            e.setKeyName(Constant.FILES_ALLOWDED.toString());
            e.setValueName(".png,.gif,.jpeg,.jpg,.xlsx,.xls,.doc,.docx,.ppt,.pptx,.txt,.pdf");
            keyValueRepository.save(e);
        }

        KeyValue fileSize = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());

        if (fileSize == null) {
            KeyValue e = new KeyValue();
            e.setDescription("file size");
            e.setKeyName(Constant.FILE_SIZE.toString());
            e.setValueName("10");
            keyValueRepository.save(e);
        }

        initDataService.existinIntake();
    }

}
