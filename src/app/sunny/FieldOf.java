package app.sunny;

import java.lang.reflect.Field;

/**
 *
 * @author laszlo
 */
public class FieldOf
{
  Field field;
  Object of;

  FieldOf( Field f, Object on )
  {
    field = f;
    of = on;
  }

}
