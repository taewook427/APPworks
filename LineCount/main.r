# test694 : LineCount with R

# install.packages("ggplot2")
# install.packages("dplyr")
library(dplyr)
library(ggplot2)
library(scales)

# ========== 설정할 값들 ==========
path = "./" # 카운트 대상 폴더 경로
code_ext = c("c", "cpp", "cs", "css", "dart", "go", "h", "hpp",
             "java", "js", "kt", "lua", "php", "py", "r", "rb",
             "rs", "sh", "ts") # 텍스트 코드 확장자
data_ext = c("csv", "htm", "html", "json", "txt", "md") # 텍스트 데이터 확장자
# ========== 설정 종료 ==========

all_files <- list.files(path = path, recursive = TRUE, full.names = TRUE)
count_lines <- function(file_path, ext) {
  if (ext %in% code_ext) {
    lines <- length( readLines(file_path) )
  } else if (ext %in% data_ext) {
    lines <- length( readLines(file_path) )
  } else {
    lines <- 0
  }
  return(lines)
} # 라인수 세기 함수

file_info <- data.frame(
  file_path = all_files,
  extension = tools::file_ext(all_files),
  file_size = file.info(all_files)$size,
  line_count = sapply( all_files, function(file) {
    ext <- tools::file_ext(file)
    count_lines(file, ext)
  } ) ) # [경로, 확장자, 크기, 라인수] 데이터프레임

# ===== 라인 수 데이터 가공 =====
temp_data <- file_info %>%
  filter(extension %in% code_ext)
temp_sum <- temp_data %>%
  group_by(extension) %>%
  summarize( total_lines = sum(line_count) )

ggplot( temp_sum, aes( x="", y=total_lines, fill=extension) ) +
  geom_bar(width = 1, stat = "identity") +
  coord_polar("y", start=0) +
  geom_text( aes(label = total_lines), position = position_stack(vjust = 0.3), size = 3 ) +
  labs(title = "확장자별 라인 수 (코드)", x = NULL, y = NULL, fill = "확장자")

temp_data <- file_info %>%
  filter(extension %in% data_ext)
temp_sum <- temp_data %>%
  group_by(extension) %>%
  summarize( total_lines = sum(line_count) )

ggplot( temp_sum, aes( x="", y=total_lines, fill=extension) ) +
  geom_bar(width = 1, stat = "identity") +
  coord_polar("y", start=0) +
  geom_text( aes(label = total_lines), position = position_stack(vjust = 0.3), size = 3 ) +
  labs(title = "확장자별 라인 수 (데이터)", x = NULL, y = NULL, fill = "확장자")

# ===== 파일 수 데이터 가공 =====
temp_sum <- file_info %>%
  group_by(extension) %>%
  summarize( file_num = n() )

ggplot( temp_sum, aes(x = extension, y = file_num, fill = extension) ) +
  geom_bar(stat = "identity") +
  geom_text(aes(label = file_num), vjust = -0.3) +
  labs(x = "확장자", y = "파일 개수", title = "확장자별 파일 개수 (전체)") +
  theme_minimal() +
  theme( axis.text.x = element_text(angle = 45, hjust = 1) )

# ===== 파일크기 데이터 가공 =====
temp_sum <- file_info %>%
  group_by(extension) %>%
  summarize( file_size = sum(file_size) )

ggplot( temp_sum, aes(x = extension, y = file_size, fill = extension) ) +
  geom_bar(stat = "identity") +
  geom_text(aes( label = comma(file_size, accuracy = 1) ), vjust = -0.3) +
  scale_y_log10() +
  labs(x = "확장자", y = "파일 크기 (log10)", title = "확장자별 파일 크기 (전체)") +
  theme_minimal() +
  theme( axis.text.x = element_text(angle = 45, hjust = 1) )
