package kz.greetgo.sqlmanager.gen;

import java.io.PrintStream;

import kz.greetgo.sqlmanager.model.Field;
import kz.greetgo.sqlmanager.model.FieldDb;
import kz.greetgo.sqlmanager.model.Table;
import kz.greetgo.sqlmanager.parser.StruShaper;

public class Nf6GeneratorPostgres extends Nf6Generator {
  
  private SqlDialect sqlDialect = null;
  
  @Override
  protected SqlDialect sqld() {
    if (sqlDialect == null) sqlDialect = new SqlDialectPostgres();
    return sqlDialect;
  }
  
  public Nf6GeneratorPostgres(Conf conf, StruShaper sg) {
    super(conf, sg);
  }
  
  @Override
  protected void printTableInsertFunction(PrintStream out, Table table) {
    out.print("create or replace function " + conf._p_ + table.name + "(");
    {
      boolean first = true;
      for (Field key : table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "" :", ");
          first = false;
          out.print(fi.name + "__ " + sqld().procType(fi.stype));
        }
      }
    }
    out.println(")");
    out.println("returns void as $" + conf.bigQuote + "$");
    out.println("begin");
    out.print("  insert into " + conf.tabPrefix + table.name + " (");
    {
      boolean first = true;
      for (Field key : table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "" :", ");
          first = false;
          out.print(fi.name);
        }
      }
    }
    out.print(") values (");
    {
      boolean first = true;
      for (Field key : table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "" :", ");
          first = false;
          out.print(fi.name + "__");
        }
      }
    }
    out.println(") ; ");
    out.println("exception when others then null ; ");
    out.println("end");
    out.println("$" + conf.bigQuote + "$ language plpgsql");
    out.println(conf.separator);
  }
  
  @Override
  protected void printFieldInsertFunction(PrintStream out, Field field) {
    String fname = conf._p_ + field.table.name + '_' + field.name;
    out.print("create or replace function " + fname + "(");
    {
      boolean first = true;
      for (Field key : field.table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "" :", ");
          first = false;
          out.print(fi.name + "__ " + sqld().procType(fi.stype));
        }
      }
      for (FieldDb fi : field.dbFields()) {
        out.print(", " + fi.name + "__ " + sqld().procType(fi.stype));
      }
    }
    out.println(")");
    out.println("returns void as $" + conf.bigQuote + "$");
    out.println("declare");
    out.println("  r record ; ");
    out.println("  doit int ; ");
    out.println("begin");
    out.println();
    out.println("  select * into r from (");
    out.print("    select m.*, row_number() over (partition by ");
    {
      boolean first = true;
      for (Field key : field.table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "" :", ");
          first = false;
          out.print("m." + fi.name);
        }
      }
    }
    out.println(" order by m." + conf.ts + " desc) as rn__");
    String tname = conf.tabPrefix + field.table.name + "_" + field.name;
    out.println("    from " + tname + " m");
    {
      boolean first = true;
      for (Field key : field.table.keys) {
        for (FieldDb fi : key.dbFields()) {
          out.print(first ? "    where " :"    and ");
          first = false;
          out.println("m." + fi.name + " = " + fi.name + "__");
        }
      }
    }
    out.println("  ) x where x.rn__ = 1 ; ");
    
    out.println();
    out.println("  doit := 0 ; ");
    
    for (FieldDb fi : field.dbFields()) {
      out.println("  if doit = 0 and r." + fi.name + " is null and " + fi.name
          + "__ is not null then doit := 1 ; end if ; ");
      out.println("  if doit = 0 and r." + fi.name + " is not null and " + fi.name
          + "__ is null then doit := 1 ; end if ; ");
      out.println("  if doit = 0 and r." + fi.name + " is not null and " + fi.name
          + "__ is not null and r. " + fi.name + " != " + fi.name + "__ then doit := 1 ; end if ; ");
      out.println();
    }
    
    out.println("  if doit = 1 then");
    printInsertSql(out, tname, field);
    out.println("  end if ; ");
    
    if (conf.genOperTables) {
      out.println();
      String oname = conf.oPref + field.table.name + "_" + field.name;
      out.print("  select count(1) into doit from " + oname);
      {
        boolean first = true;
        for (Field key : field.table.keys) {
          for (FieldDb fi : key.dbFields()) {
            out.print(first ? " where " :" and ");
            first = false;
            out.print(fi.name + " = " + fi.name + "__");
          }
        }
      }
      out.println(" ; ");
      out.println("  if doit = 0 then");
      printInsertSql(out, oname, field);
      out.println("  else");
      printUpdateSql(out, oname, field);
      out.println("  end if ; ");
    }
    
    out.println("end");
    out.println("$" + conf.bigQuote + "$ language plpgsql");
    out.println(conf.separator);
  }
  
  @Override
  protected void printPrepareSqls(PrintStream out) {
    out.println("create view dual as select 'X'::varchar as dummy" + conf.separator);
    
    out.println("create or replace function moment() returns timestamp");
    out.println("language plpgsql as 'begin return current_timestamp; end '" + conf.separator);
    
    out.println();
    
  }
  
  @Override
  protected String minus() {
    return "except";
  }
}
