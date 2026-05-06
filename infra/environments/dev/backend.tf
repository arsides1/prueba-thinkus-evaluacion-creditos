terraform {
  backend "s3" {
    bucket         = "thinkus-tfstate-DEV"
    key            = "creditos/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "thinkus-tfstate-lock"
    encrypt        = true
  }
}
