import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador reactivo de cedula ecuatoriana (algoritmo Modulo 10).
 *
 * Mismo algoritmo que aplica el backend en commons/CedulaECValidator.java.
 * Tener la validacion ESPEJO en frontend evita que el usuario llegue al
 * backend con una cedula obviamente mal formada (ahorra el round-trip).
 *
 * Reglas:
 *  1. Exactamente 10 digitos numericos.
 *  2. Provincia (primeros 2 digitos) entre 01 y 24.
 *  3. Tercer digito menor que 6 (cedulas de personas naturales).
 *  4. Coeficientes [2,1,2,1,2,1,2,1,2] sobre los primeros 9 digitos.
 *  5. Si el producto > 9, restarle 9.
 *  6. Sumar todos.
 *  7. Verificador esperado = (10 - suma % 10) % 10.
 *
 * Devuelve null si vacio (la obligatoriedad la maneja Validators.required).
 */
const COEFICIENTES = [2, 1, 2, 1, 2, 1, 2, 1, 2];

export function cedulaEcuatorianaValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = (control.value ?? '').toString().trim();

    if (!v) return null;

    if (!/^\d{10}$/.test(v)) {
      return { cedula: 'Debe contener exactamente 10 digitos' };
    }

    const provincia = parseInt(v.substring(0, 2), 10);
    if (provincia < 1 || provincia > 24) {
      return { cedula: 'Provincia invalida (debe estar entre 01 y 24)' };
    }

    if (parseInt(v[2], 10) >= 6) {
      return { cedula: 'Tercer digito invalido para persona natural' };
    }

    let suma = 0;
    for (let i = 0; i < COEFICIENTES.length; i++) {
      const producto = parseInt(v[i], 10) * COEFICIENTES[i];
      suma += producto > 9 ? producto - 9 : producto;
    }

    const verificador = (10 - (suma % 10)) % 10;
    if (verificador !== parseInt(v[9], 10)) {
      return { cedula: 'Digito verificador incorrecto' };
    }

    return null;
  };
}
