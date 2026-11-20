CREATE TABLE IF NOT EXISTS students
(
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  kana_name VARCHAR(50) NOT NULL,
  nickname VARCHAR(50),
  email VARCHAR(50) NOT NULL UNIQUE,
  area VARCHAR(50),
  age INT,
  sex VARCHAR(10),
  remark TEXT,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS students_courses
(
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  course_name VARCHAR(50) NOT NULL,
  course_start_at TIMESTAMP,
  course_end_at TIMESTAMP,
  -- 同じ受講生が同じコースを重複して登録できないようにする
  UNIQUE (student_id, course_name),
  FOREIGN KEY (student_id) REFERENCES students (id)
);