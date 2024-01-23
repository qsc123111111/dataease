CREATE TABLE "DATAEASEST"."term_table"
(
    "id" INT IDENTITY(1, 1) NOT NULL,
    "model_id" VARCHAR(200),
    "excel_id" VARCHAR(200),
    "terms" VARCHAR(1000),
    UNIQUE("id"),
    NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "MAIN", CLUSTERBTR) ;

COMMENT ON COLUMN "DATAEASEST"."term_table"."excel_id" IS 'excel数据集id';
COMMENT ON COLUMN "DATAEASEST"."term_table"."model_id" IS '模型id';
COMMENT ON COLUMN "DATAEASEST"."term_table"."terms" IS '条件json';


CREATE TABLE "DATAEASEST"."example_user"
(
    "id" INT,
    "name" VARCHAR(50),
    "age" VARCHAR(50)) STORAGE(ON "MAIN", CLUSTERBTR) ;


INSERT INTO "DATAEASEST"."example_user"("id", "name", "age")
VALUES
    (1, '张三', '25'),
    (2, '李四', '30'),
    (3, '王五', '22'),
    (4, '赵六', '28'),
    (5, '刘七', '35'),
    (6, '陈八', '29'),
    (7, '杨九', '31'),
    (8, '黄十', '26'),
    (9, '许十一', '33'),
    (10, '朱十二', '27'),
    (11, '吴十三', '24'),
    (12, '周十四', '32');


alter table "DATAEASEST"."dataset_ref" add column("ref_count" INTEGER default (1));