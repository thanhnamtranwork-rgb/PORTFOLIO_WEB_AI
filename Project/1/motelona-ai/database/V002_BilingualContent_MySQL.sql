-- Add optional Vietnamese fields to existing MySQL installations.
-- New installations already receive these columns from PortfolioAI_MySQL.sql.
USE PortfolioAI_DB;

DELIMITER $$
DROP PROCEDURE IF EXISTS AddBilingualPortfolioColumnIfMissing$$
CREATE PROCEDURE AddBilingualPortfolioColumnIfMissing(
  IN targetTable VARCHAR(64),
  IN targetColumn VARCHAR(64),
  IN targetDefinition VARCHAR(255)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = targetTable
      AND COLUMN_NAME = targetColumn
  ) THEN
    SET @bilingualDdl = CONCAT(
      'ALTER TABLE `', REPLACE(targetTable, '`', '``'),
      '` ADD COLUMN `', REPLACE(targetColumn, '`', '``'),
      '` ', targetDefinition
    );
    PREPARE bilingualStatement FROM @bilingualDdl;
    EXECUTE bilingualStatement;
    DEALLOCATE PREPARE bilingualStatement;
  END IF;
END$$
DELIMITER ;

CALL AddBilingualPortfolioColumnIfMissing('Education', 'MajorVi', 'VARCHAR(150) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Education', 'DegreeVi', 'VARCHAR(100) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Education', 'DescriptionVi', 'LONGTEXT NULL');
CALL AddBilingualPortfolioColumnIfMissing('SkillCategory', 'CategoryNameVi', 'VARCHAR(100) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Skill', 'SkillNameVi', 'VARCHAR(100) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Project', 'TitleVi', 'VARCHAR(200) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Project', 'ShortDescriptionVi', 'VARCHAR(500) NULL');
CALL AddBilingualPortfolioColumnIfMissing('Project', 'FullDescriptionVi', 'LONGTEXT NULL');

DROP PROCEDURE AddBilingualPortfolioColumnIfMissing;

-- Keep the starter categories readable in Vietnamese after upgrading an existing database.
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Giao diện người dùng') WHERE CategoryName = 'Frontend';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Phía máy chủ') WHERE CategoryName = 'Backend';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Cơ sở dữ liệu') WHERE CategoryName = 'Database';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Trí tuệ nhân tạo và học máy') WHERE CategoryName = 'AI và Machine Learning';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Công cụ') WHERE CategoryName = 'Tools và Công cụ';
