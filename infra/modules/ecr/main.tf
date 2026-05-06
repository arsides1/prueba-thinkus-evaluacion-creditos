// Modulo: ecr
// Crea los repositorios de imagenes Docker con scan-on-push y lifecycle policy.

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

resource "aws_ecr_repository" "este" {
  for_each = toset(var.repositorios)

  name                 = each.value
  image_tag_mutability = "IMMUTABLE"  // tags fijos: "1.0.0" no se puede sobrescribir

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "KMS"
  }

  tags = var.tags
}

// Lifecycle policy: mantener las ultimas 20 imagenes con tag de version,
// borrar las untagged despues de 7 dias.
resource "aws_ecr_lifecycle_policy" "limpieza" {
  for_each = aws_ecr_repository.este

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Mantener las ultimas 20 imagenes versionadas"
        selection = {
          tagStatus     = "tagged"
          tagPatternList = ["*.*.*"]
          countType     = "imageCountMoreThan"
          countNumber   = 20
        }
        action = { type = "expire" }
      },
      {
        rulePriority = 2
        description  = "Borrar imagenes sin tag despues de 7 dias"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
        action = { type = "expire" }
      }
    ]
  })
}
