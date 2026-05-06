// Backend remoto del state. La bucket S3 y la tabla DynamoDB de lock
// deben existir antes del primer terraform init (se crean a mano una sola vez).
//
// Crear backend resources (una sola vez):
//   aws s3 mb s3://thinkus-tfstate-PRD --region us-east-1
//   aws s3api put-bucket-versioning --bucket thinkus-tfstate-PRD \
//     --versioning-configuration Status=Enabled
//   aws s3api put-bucket-encryption --bucket thinkus-tfstate-PRD \
//     --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
//   aws dynamodb create-table --table-name thinkus-tfstate-lock \
//     --attribute-definitions AttributeName=LockID,AttributeType=S \
//     --key-schema AttributeName=LockID,KeyType=HASH \
//     --billing-mode PAY_PER_REQUEST --region us-east-1

terraform {
  backend "s3" {
    bucket         = "thinkus-tfstate-PRD"
    key            = "creditos/prd/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "thinkus-tfstate-lock"
    encrypt        = true
  }
}
