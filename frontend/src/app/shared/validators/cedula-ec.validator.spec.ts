import { FormControl } from '@angular/forms';

import { cedulaEcuatorianaValidator } from './cedula-ec.validator';

describe('cedulaEcuatorianaValidator', () => {
  const validator = cedulaEcuatorianaValidator();

  function validar(valor: string) {
    return validator(new FormControl(valor));
  }

  describe('valores validos (deben devolver null)', () => {
    const validas = ['1716123458', '0500000005', '0500000500'];
    validas.forEach((c) => {
      it(`acepta ${c}`, () => expect(validar(c)).toBeNull());
    });
  });

  describe('valores invalidos', () => {
    it('rechaza menos de 10 digitos', () => {
      expect(validar('123456789')?.['cedula']).toContain('10 digitos');
    });

    it('rechaza con caracteres no numericos', () => {
      expect(validar('171612345A')?.['cedula']).toBeTruthy();
    });

    it('rechaza provincia 25', () => {
      expect(validar('2516123458')?.['cedula']).toContain('Provincia');
    });

    it('rechaza tercer digito 6', () => {
      expect(validar('1766123458')?.['cedula']).toContain('Tercer digito');
    });

    it('rechaza digito verificador erroneo', () => {
      expect(validar('1716123459')?.['cedula']).toContain('verificador');
    });
  });

  describe('valores vacios', () => {
    it('null devuelve null (lo maneja Validators.required)', () => {
      expect(validar('')).toBeNull();
    });

    it('espacios en blanco devuelven null', () => {
      expect(validar('   ')).toBeNull();
    });
  });
});
