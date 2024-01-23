CREATE TABLE students ( student_id INT, student_name VARCHAR(50), class_id INT );

INSERT INTO students (student_id, student_name, class_id)
VALUES (1, '张三', 101),
       (2, '李四', 102),
       (3, '王五', 101);

CREATE TABLE classes (
                         class_id INT,
                         class_name VARCHAR(50)
);

INSERT INTO classes (class_id, class_name)
VALUES (101, '理科班'),
       (103, '文科班');


#左
SELECT *
FROM students
         LEFT JOIN classes
                   ON students.class_id = classes.class_id;

#右
SELECT *
FROM students
         RIGHT JOIN classes
                    ON students.class_id = classes.class_id;