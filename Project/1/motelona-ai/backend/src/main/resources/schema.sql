-- MySQL 8.0.36+ / 8.4 LTS. The database itself must exist before Spring Boot starts.
-- Spring Boot executes this file at startup. Every table is safe to create more than once.

CREATE TABLE IF NOT EXISTS AdminAccount (
  AdminId BIGINT NOT NULL AUTO_INCREMENT,
  Username VARCHAR(50) NOT NULL,
  Email VARCHAR(255) NOT NULL,
  PasswordHash VARCHAR(255) NOT NULL,
  DisplayName VARCHAR(100) NOT NULL,
  AccountRole VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
  IsActive BOOLEAN NOT NULL DEFAULT TRUE,
  CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UpdatedAt DATETIME NULL,
  PRIMARY KEY (AdminId), UNIQUE KEY UQ_AdminAccount_Username (Username), UNIQUE KEY UQ_AdminAccount_Email (Email),
  CONSTRAINT CK_AdminAccount_Role CHECK (AccountRole IN ('ADMIN','EDITOR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Profile (
  ProfileId TINYINT NOT NULL,
  FullName VARCHAR(100) NOT NULL,
  Headline VARCHAR(200) NULL,
  CareerObjective LONGTEXT NULL,
  AboutMe LONGTEXT NULL,
  Email VARCHAR(255) NULL,
  Phone VARCHAR(30) NULL,
  Location VARCHAR(200) NULL,
  AvatarUrl VARCHAR(500) NULL,
  CvUrl VARCHAR(500) NULL,
  GithubUrl VARCHAR(500) NULL,
  LinkedInUrl VARCHAR(500) NULL,
  WebsiteUrl VARCHAR(500) NULL,
  UpdatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (ProfileId), CONSTRAINT CK_Profile_SingleRow CHECK (ProfileId = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Education (
  EducationId BIGINT NOT NULL AUTO_INCREMENT,
  SchoolName VARCHAR(200) NOT NULL,
  Major VARCHAR(150) NULL,
  MajorVi VARCHAR(150) NULL,
  Degree VARCHAR(100) NULL,
  DegreeVi VARCHAR(100) NULL,
  StartDate DATE NULL,
  EndDate DATE NULL,
  Description LONGTEXT NULL,
  DescriptionVi LONGTEXT NULL,
  DisplayOrder INT NOT NULL DEFAULT 0,
  IsVisible BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (EducationId), KEY IX_Education_Visible_Order (IsVisible, DisplayOrder, EducationId),
  CONSTRAINT CK_Education_Dates CHECK (EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Experience (
  ExperienceId BIGINT NOT NULL AUTO_INCREMENT,
  OrganizationName VARCHAR(200) NOT NULL,
  Position VARCHAR(150) NOT NULL,
  ExperienceType VARCHAR(30) NOT NULL DEFAULT 'OTHER',
  StartDate DATE NULL,
  EndDate DATE NULL,
  Description LONGTEXT NULL,
  DisplayOrder INT NOT NULL DEFAULT 0,
  IsVisible BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (ExperienceId), KEY IX_Experience_Visible_Order (IsVisible, DisplayOrder, ExperienceId),
  CONSTRAINT CK_Experience_Type CHECK (ExperienceType IN ('INTERNSHIP','FULL_TIME','PART_TIME','FREELANCE','ACTIVITY','OTHER')),
  CONSTRAINT CK_Experience_Dates CHECK (EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS SkillCategory (
  SkillCategoryId BIGINT NOT NULL AUTO_INCREMENT,
  CategoryName VARCHAR(100) NOT NULL,
  CategoryNameVi VARCHAR(100) NULL,
  DisplayOrder INT NOT NULL DEFAULT 0,
  PRIMARY KEY (SkillCategoryId), UNIQUE KEY UQ_SkillCategory_CategoryName (CategoryName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Skill (
  SkillId BIGINT NOT NULL AUTO_INCREMENT,
  SkillCategoryId BIGINT NOT NULL,
  SkillName VARCHAR(100) NOT NULL,
  SkillNameVi VARCHAR(100) NULL,
  ProficiencyLevel TINYINT NULL,
  YearsOfExperience DECIMAL(4,1) NULL,
  DisplayOrder INT NOT NULL DEFAULT 0,
  IsVisible BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (SkillId), UNIQUE KEY UQ_Skill_Category_SkillName (SkillCategoryId, SkillName),
  KEY IX_Skill_Category_Order (SkillCategoryId, DisplayOrder),
  CONSTRAINT FK_Skill_SkillCategory FOREIGN KEY (SkillCategoryId) REFERENCES SkillCategory(SkillCategoryId),
  CONSTRAINT CK_Skill_Proficiency CHECK (ProficiencyLevel IS NULL OR ProficiencyLevel BETWEEN 0 AND 100),
  CONSTRAINT CK_Skill_Years CHECK (YearsOfExperience IS NULL OR YearsOfExperience >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Project (
  ProjectId BIGINT NOT NULL AUTO_INCREMENT,
  Title VARCHAR(200) NOT NULL,
  TitleVi VARCHAR(200) NULL,
  Slug VARCHAR(220) NOT NULL,
  ShortDescription VARCHAR(500) NULL,
  ShortDescriptionVi VARCHAR(500) NULL,
  FullDescription LONGTEXT NULL,
  FullDescriptionVi LONGTEXT NULL,
  ThumbnailUrl VARCHAR(500) NULL,
  DemoUrl VARCHAR(500) NULL,
  SourceCodeUrl VARCHAR(500) NULL,
  StartDate DATE NULL,
  EndDate DATE NULL,
  ProjectStatus VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
  IsFeatured BOOLEAN NOT NULL DEFAULT FALSE,
  IsPublished BOOLEAN NOT NULL DEFAULT FALSE,
  DisplayOrder INT NOT NULL DEFAULT 0,
  CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UpdatedAt DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (ProjectId), UNIQUE KEY UQ_Project_Slug (Slug),
  KEY IX_Project_Published_Order (IsPublished, IsFeatured, DisplayOrder, CreatedAt),
  CONSTRAINT CK_Project_Status CHECK (ProjectStatus IN ('PLANNED','IN_PROGRESS','COMPLETED','ARCHIVED')),
  CONSTRAINT CK_Project_Dates CHECK (EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS Technology (
  TechnologyId BIGINT NOT NULL AUTO_INCREMENT,
  TechnologyName VARCHAR(100) NOT NULL,
  TechnologyType VARCHAR(50) NULL,
  PRIMARY KEY (TechnologyId), UNIQUE KEY UQ_Technology_TechnologyName (TechnologyName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ProjectTechnology (
  ProjectId BIGINT NOT NULL,
  TechnologyId BIGINT NOT NULL,
  PRIMARY KEY (ProjectId, TechnologyId),
  CONSTRAINT FK_ProjectTechnology_Project FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE,
  CONSTRAINT FK_ProjectTechnology_Technology FOREIGN KEY (TechnologyId) REFERENCES Technology(TechnologyId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ContactMessage (
  MessageId BIGINT NOT NULL AUTO_INCREMENT,
  SenderName VARCHAR(120) NOT NULL,
  SenderEmail VARCHAR(255) NOT NULL,
  Subject VARCHAR(200) NULL,
  MessageBody LONGTEXT NOT NULL,
  ReceivedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  MessageStatus VARCHAR(20) NOT NULL DEFAULT 'NEW',
  RepliedAt DATETIME NULL,
  RepliedByAdminId BIGINT NULL,
  PRIMARY KEY (MessageId), KEY IX_ContactMessage_Status_Date (MessageStatus, ReceivedAt),
  CONSTRAINT FK_ContactMessage_AdminAccount FOREIGN KEY (RepliedByAdminId) REFERENCES AdminAccount(AdminId) ON DELETE SET NULL,
  CONSTRAINT CK_ContactMessage_Status CHECK (MessageStatus IN ('NEW','READ','REPLIED','ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS AIConversation (
  ConversationId BIGINT NOT NULL AUTO_INCREMENT,
  SessionKey CHAR(36) NOT NULL,
  SessionId VARCHAR(100) NULL,
  StartedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  LastMessageAt DATETIME NULL,
  SourcePage VARCHAR(300) NULL,
  IsArchived BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (ConversationId), UNIQUE KEY UQ_AIConversation_SessionKey (SessionKey),
  UNIQUE KEY UX_AIConversation_SessionId (SessionId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS AIMessage (
  AIMessageId BIGINT NOT NULL AUTO_INCREMENT,
  ConversationId BIGINT NOT NULL,
  SenderRole VARCHAR(10) NOT NULL,
  MessageContent LONGTEXT NOT NULL,
  ModelName VARCHAR(100) NULL,
  InputTokens INT NULL,
  OutputTokens INT NULL,
  CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (AIMessageId), KEY IX_AIMessage_Conversation_Date (ConversationId, CreatedAt),
  CONSTRAINT FK_AIMessage_AIConversation FOREIGN KEY (ConversationId) REFERENCES AIConversation(ConversationId) ON DELETE CASCADE,
  CONSTRAINT CK_AIMessage_SenderRole CHECK (SenderRole IN ('SYSTEM','USER','ASSISTANT')),
  CONSTRAINT CK_AIMessage_InputTokens CHECK (InputTokens IS NULL OR InputTokens >= 0),
  CONSTRAINT CK_AIMessage_OutputTokens CHECK (OutputTokens IS NULL OR OutputTokens >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS AIKnowledgeItem (
  KnowledgeId BIGINT NOT NULL AUTO_INCREMENT,
  Title VARCHAR(200) NOT NULL,
  Category VARCHAR(100) NULL,
  Content LONGTEXT NOT NULL,
  Keywords VARCHAR(500) NULL,
  SourceUrl VARCHAR(500) NULL,
  IsActive BOOLEAN NOT NULL DEFAULT TRUE,
  UpdatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (KnowledgeId), KEY IX_AIKnowledgeItem_Active_Category (IsActive, Category, UpdatedAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS SiteSetting (
  SettingKey VARCHAR(100) NOT NULL,
  SettingValue LONGTEXT NULL,
  Description VARCHAR(300) NULL,
  IsPublic BOOLEAN NOT NULL DEFAULT TRUE,
  UpdatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (SettingKey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- MoteLona privacy-controlled conversation history and long-term memory.
CREATE TABLE IF NOT EXISTS conversation_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  content LONGTEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_conversation_session_id (session_id, id DESC),
  CONSTRAINT CK_conversation_role CHECK (role IN ('user','assistant'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Persistent MoteLona model-call quota. One row per Vietnam calendar day.
CREATE TABLE IF NOT EXISTS ai_api_daily_usage (
  usage_date DATE NOT NULL,
  request_count INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (usage_date),
  CONSTRAINT CK_ai_api_daily_usage_count CHECK (request_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_memories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id VARCHAR(100) NOT NULL,
  content VARCHAR(800) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_user_memories_session_id (session_id, id DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed only the starter profile and skill categories; no password or API key is seeded.
INSERT INTO Profile (ProfileId, FullName, Email, Headline)
SELECT 1, 'Trần Thành Nam', 'thanhnamtran.work@gmail.com', 'Sinh viên Công nghệ thông tin'
WHERE NOT EXISTS (SELECT 1 FROM Profile WHERE ProfileId = 1);

INSERT INTO SkillCategory (CategoryName, CategoryNameVi, DisplayOrder)
SELECT 'Frontend', 'Giao diện người dùng', 1 WHERE NOT EXISTS (SELECT 1 FROM SkillCategory WHERE CategoryName = 'Frontend');
INSERT INTO SkillCategory (CategoryName, CategoryNameVi, DisplayOrder)
SELECT 'Backend', 'Phía máy chủ', 2 WHERE NOT EXISTS (SELECT 1 FROM SkillCategory WHERE CategoryName = 'Backend');
INSERT INTO SkillCategory (CategoryName, CategoryNameVi, DisplayOrder)
SELECT 'Database', 'Cơ sở dữ liệu', 3 WHERE NOT EXISTS (SELECT 1 FROM SkillCategory WHERE CategoryName = 'Database');
INSERT INTO SkillCategory (CategoryName, CategoryNameVi, DisplayOrder)
SELECT 'AI và Machine Learning', 'Trí tuệ nhân tạo và học máy', 4 WHERE NOT EXISTS (SELECT 1 FROM SkillCategory WHERE CategoryName = 'AI và Machine Learning');
INSERT INTO SkillCategory (CategoryName, CategoryNameVi, DisplayOrder)
SELECT 'Tools và Công cụ', 'Công cụ', 5 WHERE NOT EXISTS (SELECT 1 FROM SkillCategory WHERE CategoryName = 'Tools và Công cụ');

UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Giao diện người dùng') WHERE CategoryName = 'Frontend';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Phía máy chủ') WHERE CategoryName = 'Backend';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Cơ sở dữ liệu') WHERE CategoryName = 'Database';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Trí tuệ nhân tạo và học máy') WHERE CategoryName = 'AI và Machine Learning';
UPDATE SkillCategory SET CategoryNameVi = COALESCE(CategoryNameVi, 'Công cụ') WHERE CategoryName = 'Tools và Công cụ';
