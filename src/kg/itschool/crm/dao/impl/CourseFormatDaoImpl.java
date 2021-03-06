package kg.itschool.crm.dao.impl;

import kg.itschool.crm.dao.CourseFormatDao;
import kg.itschool.crm.dao.daoutil.Log;
import kg.itschool.crm.model.CourseFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CourseFormatDaoImpl implements CourseFormatDao {

    public CourseFormatDaoImpl() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            Log.info(this.getClass().getSimpleName(), Connection.class.getSimpleName(), "Establishing connection");
            connection = getConnection();
            Log.info(this.getClass().getSimpleName(), Connection.class.getSimpleName(), "Connection established");

            String ddlQuery = "CREATE TABLE IF NOT EXISTS tb_course_format(" +
                    "id BIGSERIAL, " +
                    "course_format VARCHAR(50) NOT NULL, " +
                    "course_duration_weeks INT NOT NULL, " +
                    "lesson_duration TIME NOT NULL, " +
                    "lessons_per_week INT NOT NULL, " +
                    "is_online BOOLEAN NOT NULL, " +
                    "date_created TIMESTAMP NOT NULL DEFAULT NOW(), " +
                    "" +
                    "CONSTRAINT pk_course_format_id PRIMARY KEY(id), " +
                    "CONSTRAINT course_duration_weeks_negative_or_zero CHECK (course_duration_weeks > 0), " +
                    "CONSTRAINT lesson_per_week_negative_or_zero CHECK (lessons_per_week > 0)" +
                    ");";

            Log.info(this.getClass().getSimpleName(), PreparedStatement.class.getSimpleName(), "Creating preparedStatement");
            preparedStatement = connection.prepareStatement(ddlQuery);

            preparedStatement.execute();
        } catch (SQLException e) {
            Log.error(this.getClass().getSimpleName(), e.getStackTrace()[0].getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        } finally {
            close(preparedStatement);
            close(connection);
        }
    }

    @Override
    public CourseFormat save(CourseFormat courseFormat) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try{
            System.out.println("Connecting to database...");
            connection = getConnection();
            System.out.println("Connection succeeded.");

            String createQuery = "INSERT INTO tb_groups(" +
                    "course_format, course_duration , is_online, date_created) " +

                    "VALUES(?, ?, ?, ?)";


            preparedStatement = connection.prepareStatement(createQuery);
            preparedStatement.setString(1 , courseFormat.getFormat());
            preparedStatement.setInt(2, courseFormat.getCourseDurationWeeks());
            preparedStatement.setBoolean(3, courseFormat.isOnline());
            preparedStatement.setDate(4 , courseFormat.getDateCreated());

            preparedStatement.execute(createQuery);




        }catch(SQLException e){
            e.printStackTrace();

        }final {
            close(resultSet);
            close(preparedStatement);
            close(connection);

        }
    }

    @Override
    public CourseFormat findById(Long id) {
        return null;
    }
}
