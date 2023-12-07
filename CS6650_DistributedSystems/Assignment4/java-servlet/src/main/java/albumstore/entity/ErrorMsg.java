package albumstore.entity;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;

/**
 * ErrorMsg
 */
public class ErrorMsg {
    @SerializedName("msg")
    private String msg = null;

    public ErrorMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Get msg
     * @return msg
     **/
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorMsg errorMsg = (ErrorMsg) o;
        return Objects.equals(this.msg, errorMsg.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorMsg {\n");

        sb.append("    msg: ").append(toIndentedString(msg)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

