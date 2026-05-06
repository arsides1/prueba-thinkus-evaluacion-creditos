// Modulo: sqs
// Cola SQS con DLQ asociada. Pensada para eventos asincronos (futuro,
// cuando el orquestador publique eventos OrderEvaluated u OrderRejected).

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

resource "aws_sqs_queue" "dlq" {
  name                      = "${var.nombre}-dlq"
  message_retention_seconds = 1209600  // 14 dias (maximo)

  tags = var.tags
}

resource "aws_sqs_queue" "principal" {
  name                       = var.nombre
  visibility_timeout_seconds = var.visibility_timeout
  message_retention_seconds  = 345600   // 4 dias
  receive_wait_time_seconds  = 20       // long polling para reducir empty-receives

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 5
  })

  tags = var.tags
}
