package edu.northeastern.cs5500.service;

import edu.northeastern.cs5500.model.StudentHomeWork;
import edu.northeastern.cs5500.repository.StudentHomeWorkRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Praveen Singh
 * upload Assignment Service Class.
 */
@Service
public class UploadAssignmentService {
    private static final Logger LOGGER = LogManager.getLogger(UploadAssignmentService.class);

    private final StudentHomeWorkRepository studentHomeWorkRepository;

    private final Environment env;

    @Autowired
    public UploadAssignmentService(StudentHomeWorkRepository studentHomeWorkRepository, Environment env) {
        this.studentHomeWorkRepository = studentHomeWorkRepository;
        this.env = env;
    }

    /**
     * Method to upload assignment file and update the StudentHomeWork table
     * with the given uploaded file path.
     * @param file Multipart file
     * @param userId integer
     * @param courseId Integer
     * @param hwId Integer
     * @throws IOException In case file is not readable.
     */
    public void uploadAssignment(MultipartFile file, int userId, int courseId, int hwId) throws IOException {

        LOGGER.info("Uploading assignment for user {}", userId);
        String filepath;

        // Get the filename and build the local file path
        String filename = file.getOriginalFilename();
        String directory = env.getProperty("fileupload.paths.uploadedFiles");
        String path = String.format("%s/%d/%d/%d", directory, userId, courseId, hwId);
        filepath = Paths.get(path, filename).toString();

        File fileUpload = new File(filepath);
        Files.deleteIfExists(fileUpload.toPath());
        fileUpload.getParentFile().mkdirs();

        // Save the file locally
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileUpload))) {
            stream.write(file.getBytes());
            uploadAssignmentToDatabase(filepath, userId, courseId, hwId);
        }

    }
    
    /**
     * Method to upload assignment file and update the StudentHomeWork table
     * with the given file in byte array and file name.
     * @param byte[] file in byte array
     * @param String name of file
     * @param userId integer
     * @param courseId Integer
     * @param hwId Integer
     * @throws IOException In case file is not readable.
     */
    public void uploadAssignment(byte[] fileBytes, String filename, int userId, int courseId, int hwId)  throws IOException {
    	
    	LOGGER.info("Uploading assignment for user {}", userId);
        String filepath;
    	
    	String directory = env.getProperty("fileupload.paths.uploadedFiles");
    	String path = String.format("%s/%d/%d/%d", directory, userId, courseId, hwId);
    	filepath = Paths.get(path, filename).toString();

        File fileUpload = new File(filepath);
        Files.deleteIfExists(fileUpload.toPath());
        fileUpload.getParentFile().mkdirs();
        
        // Save the file locally
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileUpload))) {
            stream.write(fileBytes);
            uploadAssignmentToDatabase(filepath, userId, courseId, hwId);
        }
    }

    private StudentHomeWork createStudentHomeWorkEntity(int userId, int courseId,
                                                        int hwId, String path){
        return new StudentHomeWork()
                .withUserId(userId)
                .withCourseId(courseId)
                .withHomeWorkId(hwId)
                .withPath(path);
    }

    private void uploadAssignmentToDatabase(String filepath, int userId, int courseId, int hwId){
        List<StudentHomeWork> studentHomeWorkList = studentHomeWorkRepository
                .findByUserIdAndCourseIdAndHomeWorkId(userId, courseId, hwId);
        StudentHomeWork studentHomeWork;
        if(studentHomeWorkList.isEmpty()) {
            studentHomeWork = createStudentHomeWorkEntity(userId,
                    courseId, hwId, filepath);
        }else{
            studentHomeWork = studentHomeWorkList.get(0).withPath(filepath);
        }
        studentHomeWorkRepository.save(studentHomeWork);
    }

}
