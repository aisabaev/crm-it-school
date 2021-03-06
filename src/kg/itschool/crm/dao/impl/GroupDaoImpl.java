package kg.itschool.crm.dao.impl;

import kg.itschool.crm.dao.GroupDao;
import kg.itschool.crm.model.Course;
import kg.itschool.crm.model.CourseFormat;
import kg.itschool.crm.model.Group;
import kg.itschool.crm.model.Mentor;

import java.sql.*;
import java.time.LocalTime;

public class GroupDaoImpl implements GroupDao {

    public GroupDaoImpl() {
        Connection connection = null;
        Statement statement = null;

        try {
            System.out.println("Connecting to database...");
            connection = getConnection();
            System.out.println("Connection succeeded.");

            String ddlQuery = "CREATE TABLE IF NOT EXISTS tb_groups(" +
                    "id           BIGSERIAL, " +
                    "name       VARCHAR(50)  NOT NULL, " +
                    "group_time  TIMESTAMP   NOT NULL, " +
                    "date_created TIMESTAMP    NOT NULL DEFAULT NOW(), " +
                    "course_id BIGINT NOT NULL, " +
                    "mentor_id BIGINT NOT NULL, " +
                    "" +
                    "CONSTRAINT pk_group_id PRIMARY KEY(id)," +
                    "CONSTRAINT fk_course_id FOREIGN KEY (course_id) " +
                    "   REFERENCES tb_courses (id), " +
                    "CONSTRAINT fk_mentor_id FOREIGN KEY (mentor_id) " +
                    "   REFERENCES tb_mentors (id)" +
                    ");";

            System.out.println("Creating statement...");
            statement = connection.createStatement();
            System.out.println("Executing create table statement...");
            statement.execute(ddlQuery);
            System.out.println(ddlQuery);

        } catch (SQLException e) {
            System.out.println("Some error occurred");
            e.printStackTrace();
        } finally {
            close(statement);
            close(connection);
        }
    }

    @Override
    public Group save(Group group) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Group savedGroup = null;
        Course savedCourse = null;
        CourseFormat savedCourseFormat = null;
        Mentor savedMentor = null;


        try {
            System.out.println("Connecting to database...");
            connection = getConnection();
            System.out.println("Connection succeeded.");

            String createQuery = "INSERT INTO tb_groups(" +
                    "name, group_time, date_created, mentor_id, course_id) " +

                    "VALUES(?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(createQuery);
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, String.valueOf(group.getGroupTime()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(group.getDateCreated()));
            preparedStatement.setLong(4, group.getMentor().getId());
            preparedStatement.setLong(5, group.getCourse().getId());

            preparedStatement.execute();
            close(preparedStatement);




            String subQuery = "SELECT c.id AS course_id, c.name, c.price, c.date_created, " +
                    "f.id AS format_id, f.course_format, f.course_duration_weeks, f.lesson_duration, " +
                    "f.lessons_per_week, f.is_online, f.date_created AS format_dc " +
                    "FROM tb_course AS c " +
                    "JOIN tb_course_format AS f " +
                    "ON c.course_format_id = f.id";

            String readQuery = "" +
                    "SELECT g.id AS group_id, g.name, g.group_time, " +
                    "g.date_created AS group_dc, g.course_id, g.mentor_id, " +
                    "m.id, m.first_name, m.last_name, m.email, m.phone_number" +
                    "m.salary, m.dob, m.date_created " +
                    "FROM tb_groups AS g " +
                    "JOIN (" + subQuery + " WHERE course_id = g.course_id) AS c " +
                    "ON g.course_id = c.id " +
                    "JOIN tb_mentor AS m " +
                    "ON g.mentor_id = m.id " +
                    "ORDER BY g.id DESC " +
                    "LIMIT 1";

            preparedStatement = connection.prepareStatement(readQuery);
            resultSet = preparedStatement.executeQuery();

            resultSet.next();

            savedMentor = new Mentor();
            savedMentor.setId(resultSet.getLong("m.id"));
            savedMentor.setFirstName(resultSet.getString("m.first_name"));
            savedMentor.setLastName(resultSet.getString("m.last_name"));
            savedMentor.setEmail(resultSet.getString("m.email"));
            savedMentor.setPhoneNumber(resultSet.getString("m.phone_number"));
            savedMentor.setSalary(Double.parseDouble(resultSet.getString("m.salary")));
            savedMentor.setDob(resultSet.getDate("m.dob").toLocalDate());
            savedMentor.setDateCreated(resultSet.getTimestamp("m.date_created").toLocalDateTime());


            savedCourseFormat =  new CourseFormat();
            savedCourseFormat.setId(resultSet.getLong("format_id"));
            savedCourseFormat.setFormat(resultSet.getString("f.course_format"));
            savedCourseFormat.setLessonDuration(resultSet.getTime("f.lesson_duration").toLocalTime());
            savedCourseFormat.setOnline(resultSet.getBoolean("f.is_online"));
            savedCourseFormat.setDateCreated(resultSet.getTimestamp("f.date_created").toLocalDateTime());
            savedCourseFormat.setCourseDurationWeeks(resultSet.getInt("f.course_duration_weeks"));
            savedCourseFormat.setLessonsPerWeek(resultSet.getInt("f.lessons_per_week"));


            savedCourse = new Course();
            savedCourse.setId(resultSet.getLong("course_id"));
            savedCourse.setPrice(Double.parseDouble(resultSet.getString("c.price")));
            savedCourse.setName(resultSet.getString("c.name"));
            savedCourse.setDateCreated(resultSet.getTimestamp("c.date_created").toLocalDateTime());
            savedCourse.setCourseFormat(savedCourseFormat);



            savedGroup = new Group();
            savedGroup.setId(resultSet.getLong("id"));
            savedGroup.setName(resultSet.getString("name"));
            savedGroup.setGroupTime(LocalTime.parse(resultSet.getString("group_time")));
            savedGroup.setDateCreated(resultSet.getTimestamp("date_created").toLocalDateTime());
            savedGroup.setCourse(savedCourse);
            savedGroup.setMentor(savedMentor);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
        return savedGroup;
    }

    @Override
    public Group findById(Long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Group savedGroup = null;
        Mentor savedMentor = null;
        CourseFormat savedCourseFormat = null;
        Course savedCourse = null;
        try {
            connection = getConnection();

            String subQuery = "SELECT c.id AS course_id, c.name, c.price, c.date_created, " +
                    "f.id AS format_id, f.course_format, f.course_duration_weeks, f.lesson_duration, " +
                    "f.lessons_per_week, f.is_online, f.date_created AS format_dc " +
                    "FROM tb_course AS c " +
                    "JOIN tb_course_format AS f " +
                    "ON c.course_format_id = f.id";

            String readQuery = "" +
                    "SELECT g.id AS group_id, g.name, g.group_time, " +
                    "g.date_created AS group_dc, g.course_id, g.mentor_id, " +
                    "m.id, m.first_name, m.last_name, m.email, m.phone_number" +
                    "m.salary, m.dob, m.date_created " +
                    "FROM tb_groups AS g " +
                    "JOIN (" + subQuery + " WHERE course_id = g.course_id) AS c " +
                    "ON g.course_id = c.id " +
                    "JOIN tb_mentor AS m " +
                    "ON g.mentor_id = m.id " +
                    "ORDER BY g.id WHERE g.id = ? ";


            preparedStatement = connection.prepareStatement(readQuery);
            preparedStatement.setLong(1, id);

            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            savedMentor = new Mentor();
            savedMentor.setId(resultSet.getLong("m.id"));
            savedMentor.setFirstName(resultSet.getString("m.first_name"));
            savedMentor.setLastName(resultSet.getString("m.last_name"));
            savedMentor.setEmail(resultSet.getString("m.email"));
            savedMentor.setPhoneNumber(resultSet.getString("m.phone_number"));
            savedMentor.setSalary(Double.parseDouble(resultSet.getString("m.salary")));
            savedMentor.setDob(resultSet.getDate("m.dob").toLocalDate());
            savedMentor.setDateCreated(resultSet.getTimestamp("m.date_created").toLocalDateTime());


            savedCourseFormat =  new CourseFormat();
            savedCourseFormat.setId(resultSet.getLong("format_id"));
            savedCourseFormat.setFormat(resultSet.getString("f.course_format"));
            savedCourseFormat.setLessonDuration(resultSet.getTime("f.lesson_duration").toLocalTime());
            savedCourseFormat.setOnline(resultSet.getBoolean("f.is_online"));
            savedCourseFormat.setDateCreated(resultSet.getTimestamp("f.date_created").toLocalDateTime());
            savedCourseFormat.setCourseDurationWeeks(resultSet.getInt("f.course_duration_weeks"));
            savedCourseFormat.setLessonsPerWeek(resultSet.getInt("f.lessons_per_week"));


            savedCourse = new Course();
            savedCourse.setId(resultSet.getLong("course_id"));
            savedCourse.setPrice(Double.parseDouble(resultSet.getString("c.price")));
            savedCourse.setName(resultSet.getString("c.name"));
            savedCourse.setDateCreated(resultSet.getTimestamp("c.date_created").toLocalDateTime());
            savedCourse.setCourseFormat(savedCourseFormat);



            savedGroup = new Group();
            savedGroup.setId(resultSet.getLong("id"));
            savedGroup.setName(resultSet.getString("name"));
            savedGroup.setGroupTime(LocalTime.parse(resultSet.getString("group_time")));
            savedGroup.setDateCreated(resultSet.getTimestamp("date_created").toLocalDateTime());
            savedGroup.setCourse(savedCourse);
            savedGroup.setMentor(savedMentor);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
        return savedGroup;
    }
}
